package de.tum.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/youtube")
public class YouTubeController {


	private static final String VIDEO_INDEX_NAME = "videos";
	private static final String SHORTS_INDEX_NAME = "shorts";
	private static final Long MAX_RESULTS = 50L; // 0 to 50, default 5
	private static final String REGION_CODE = "DE"; // ISO 3166-1 alpha-2 country code
	private static final String DEFAULT_SEARCH_TERM = "#shorts ";

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	private YouTube youtube;

	@Autowired
	private APIController apiController;

	public YouTubeController() {

		youtube = new YouTube.Builder(
			new NetHttpTransport(),
			new JacksonFactory(),
			request -> {
			}
		).setApplicationName("RS").build();
	}

	@GetMapping("/mostPopular")
	public List<Video> getMostPopularVideos() throws IOException {

		YouTube.Videos.List request = youtube.videos().list(
			Collections.singletonList("snippet,statistics"));
		request.setChart("mostPopular");
		request.setMaxResults(MAX_RESULTS);
		request.setKey(apiController.getCurrentApiKey());
		request.setRegionCode(REGION_CODE);

		VideoListResponse response = request.execute();
		response.getItems().forEach(this::save);
		log.info("Found {} most popular videos", response.getItems().size());
		return response.getItems();
	}


	@GetMapping("/search")
	public List<Video> searchVideos(@Parameter(example = "#shorts db") String keyword,
		@Nullable @Parameter(example = "2023-12-01") String after,
		@Nullable @Parameter(example = "2023-12-08") String before) throws IOException {

		// create a request to search for videos
		YouTube.Search.List request = youtube.search().list(Collections.singletonList("snippet"));
		request.setQ(keyword);
		request.setKey(apiController.getCurrentApiKey());
		request.setMaxResults(MAX_RESULTS);
		request.setType(Collections.singletonList("video"));
		request.setVideoEmbeddable("true");
		request.setVideoDuration("short");
		request.setRegionCode(REGION_CODE);

		if (after != null && !after.equals("")) {
			request.setPublishedAfter(after + "T00:00:00Z");
		}
		if (before != null && !before.equals("")) {
			request.setPublishedBefore(before + "T00:00:00Z");
		}

		// execute the request and get the video IDs
		SearchListResponse response = request.execute();
		List<String> videoIds = response.getItems().stream()
			.map(item -> item.getId().getVideoId())
			.collect(Collectors.toList());
		log.info("Found {} videos for keyword {}, between {} and {}", videoIds.size(), keyword,
			after, before);
		return getDetails(videoIds);
	}

	@GetMapping("/searchBatch")
	@Operation(summary = "Batch Video Search", description = "Search for videos based on multiple keywords. Keywords are separated by '|'. Returns a combined list of videos matching any of the specified keywords.")
	public List<Video> searchVideosBatch(
		@Parameter(name = "prefix", description = "prefix for every keyword", example = "#shorts") @Nullable String prefix,
		@Parameter(example = "Comedy | Challenge | DIY") String keywords,
		@Parameter(example = "2023-01-01") String after,
		@Parameter(example = "2023-02-01") String before,
		@Parameter(example = "2", description = "The number of intervals to divide the time range between 'after' and 'before' dates. This determines how many times the search will be performed for each keyword within the specified date range, each time covering a different time interval.") int times)
		throws IOException, ParseException {
		List<Video> videos = new LinkedList<>();
		String[] keywordArray = keywords.split("\\|");
		prefix = prefix == null || prefix.equals("") ? "" : prefix + " ";

		// calculate date ranges based on the given parameters after, before and times
		List<String[]> dateRanges = calculateDateRanges(after, before, times);
		for (String keyword : keywordArray) {
			keyword = keyword.trim();
			for (String[] dateRange : dateRanges) {
				try {
					String currentAfter = dateRange[0];
					String currentBefore = dateRange[1];
					List<Video> videosForKeyword = this.searchVideos(prefix + keyword, currentAfter,
						currentBefore);
					videos.addAll(videosForKeyword);
				} catch (GoogleJsonResponseException e) {
					boolean isApiKeyExhausted = e.getMessage()
						.contains("The request cannot be completed because you have exceeded your");
					if (isApiKeyExhausted) {
						apiController.switchToNextKey();
						log.warn("API key exhausted, switching to next key: {}", apiController.getCurrentApiKey());

						// retry the request with the next API key
						String currentAfter = dateRange[0];
						String currentBefore = dateRange[1];
						List<Video> videosForKeyword = this.searchVideos(prefix + keyword, currentAfter,
							currentBefore);
						videos.addAll(videosForKeyword);
					}
				}
			}
		}
		return videos;
	}


	public List<Video> getDetails(List<String> videoIds) throws IOException {
		if (videoIds == null || videoIds.size() == 0) {
			return new ArrayList<>();
		}
		YouTube.Videos.List request = youtube.videos()
			.list(Collections.singletonList("snippet,topicDetails,statistics,status,contentDetails"));
		request.setId(videoIds);
		request.setKey(apiController.getCurrentApiKey());

		try {
			VideoListResponse response = request.execute();

			// save all the videos in elastic
			response.getItems().forEach(this::save);

			return response.getItems();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return new ArrayList<>();
	}

	private List<String[]> calculateDateRanges(String after, String before, int times)
		throws ParseException {
		times = times < 1 ? 1 : times;
		List<String[]> dateRanges = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(after);
		Date endDate = dateFormat.parse(before);
		long timeDiff = endDate.getTime() - startDate.getTime();
		long timeInterval = timeDiff / times;

		for (int i = 0; i < times; i++) {
			Date newStart = new Date(startDate.getTime() + (timeInterval * i));
			Date newEnd = new Date(startDate.getTime() + (timeInterval * (i + 1)));
			String[] range = new String[]{
				dateFormat.format(newStart),
				dateFormat.format(newEnd)
			};
			dateRanges.add(range);
		}
		return dateRanges;
	}


	private void save(Video video) {
		// create a new index in elastic
		// Check if the index exists
		boolean indexExists = elasticsearchOperations.indexOps(
			IndexCoordinates.of(VIDEO_INDEX_NAME)).exists();

		if (!indexExists) {
			// If the index doesn't exist, create it
			elasticsearchOperations.indexOps(IndexCoordinates.of(VIDEO_INDEX_NAME)).create();
			log.info("Created index: " + VIDEO_INDEX_NAME);
		}
		// save the video in elastic
		elasticsearchOperations.save(video, IndexCoordinates.of(VIDEO_INDEX_NAME));

//		if(video.getSnippet().getTags() != null
//			&& video.getSnippet().getTags().size() > 0) {
//			elasticsearchOperations.save(video, IndexCoordinates.of(SHORTS_INDEX_NAME));
//		}


//		saveToLocal(video); // save the video in local folder

	}

	// For Test: save it into the local folder videos
	private void saveToLocal(Video video) {
		try {
			// Define the directory path
			String directoryPath = "videos"; // Replace with actual path
			File directory = new File(directoryPath);
			if (!directory.exists()) {
				directory.mkdirs(); // Create directory if it does not exist
			}

			// Convert the video object to JSON
			ObjectMapper objectMapper = new ObjectMapper();
			String videoJson = objectMapper.writeValueAsString(video);

			// Define the file name (using video ID or title)
			String fileName = video.getId() + ".json"; // Replace with actual property

			// Write to file
			File file = new File(directory, fileName);
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(videoJson);
			}
			log.info("Saved video to local folder: " + fileName);
		} catch (IOException e) {
			log.error("Error saving video to local folder", e);
		}
	}
}
