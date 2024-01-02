package de.tum.rs.controller;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.repository.VideoRepository;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Log4j2
@RestController
@RequestMapping("/test")
public class TestController {

	@Autowired
	VideoRepository videoRepository;

	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Value("${spring.elasticsearch.username}")
	private String username;

	@Value("${spring.elasticsearch.password}")
	private String password;


	private RestHighLevelClient createClient() {
		String[] uriParts = elasticsearchUri.split("://");
		String scheme = uriParts[0]; // "http"
		String[] hostParts = uriParts[1].split(":");
		String host = hostParts[0]; // "131.159.25.96"
		int port = Integer.parseInt(hostParts[1]); // 443

		// Set the credentials
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
			new UsernamePasswordCredentials(username, password));

		// Build the client with the credentials provider
		RestClientBuilder builder = RestClient.builder(
			new HttpHost(host, port, scheme))
			.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(
					HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			});

		// Build the high-level client using the low-level client configuration
		return new RestHighLevelClient(builder);
	}

	@PostMapping("/uploadVideos")
	public void uploadData(@Parameter(example = "/Users/elaine/Downloads/videos") @RequestParam String folderPath) {
		try {
			long count = videoRepository.count();
			// for each video in the folder
			Files.walk(Paths.get(folderPath))
				.filter(Files::isRegularFile)
				.forEach(file -> {
					try {
						// Parse JSON file
						YouTubeVideo video = new ObjectMapper().readValue(file.toFile(), YouTubeVideo.class);
						videoRepository.save(video);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			log.info("Finished transferring {} videos", videoRepository.count() - count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("downloadVideos")
	public void downloadData(@Parameter(example = "/Users/elaine/Downloads/videos") @RequestParam String folderPath)
		throws IOException {

		RestHighLevelClient client = createClient();
		final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
		SearchRequest searchRequest = new SearchRequest("videos");
		searchRequest.scroll(scroll);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(matchAllQuery());
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		String scrollId = searchResponse.getScrollId();
		SearchHit[] searchHits = searchResponse.getHits().getHits();

		while (searchHits != null && searchHits.length > 0) {

			SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
			scrollRequest.scroll(scroll);
			searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
			scrollId = searchResponse.getScrollId();
			searchHits = searchResponse.getHits().getHits();
			for(SearchHit hit : searchHits) {
				String videoJson = hit.getSourceAsString();
				YouTubeVideo video = new ObjectMapper().readValue(videoJson, YouTubeVideo.class);
				saveVideo(video, folderPath);
			}
		}

		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest,
			RequestOptions.DEFAULT);
		boolean succeeded = clearScrollResponse.isSucceeded();
	}

	private void saveVideo(YouTubeVideo video, String filepath) {
		try {
			// Define the directory path
			String directoryPath = filepath; // Replace with actual path
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
			log.info("Saved video to local folder: " + directoryPath + "/" + fileName);
		} catch (IOException e) {
			log.error("Error saving video to local folder", e);
		}
	}


}
