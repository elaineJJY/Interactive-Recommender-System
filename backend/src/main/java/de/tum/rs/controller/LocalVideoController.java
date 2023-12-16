package de.tum.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.youtube.model.Video;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.repository.VideoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/videos")
public class LocalVideoController {

	@Autowired
	VideoRepository videoRepository;

	private Random random = new Random();

	@GetMapping("/recommendations")
	@Operation(summary = "Get recommendations using model", description = "Returns recommendations stored in the database.")
	public List<YouTubeVideo> getRecommendations(String userId) {
		int page = random.nextInt(100);
		Page<YouTubeVideo> videoPage = videoRepository.findAll(PageRequest.of(page, 20));
		List<YouTubeVideo> videos = videoPage.getContent();
		log.info("Got {} recommendations", videos.size());
		return videos;
	}

	@GetMapping("/all")
	@Operation(summary = "Get videos from elastic search in pages", description = "Returns all videos stored in the database.")
	public List<YouTubeVideo> getAllVideos(int page, int size) {
		List<YouTubeVideo> videos = videoRepository.findAll(PageRequest.of(page, size)).getContent();

		log.info("Found {} videos", videos.size());
		return videos;
	}

	@GetMapping("/download")
	// save it into the local folder videos
	@Operation(summary = "Download all videos from elastic search", description = "Downloads all videos stored in the database.")
	public void download(@Parameter(example = "/Users/elaine/Downloads/") String filepath) {
		final int pageSize = 100;
		Pageable pageable = PageRequest.of(0, pageSize);
		Page<YouTubeVideo> page;
		do {
			page = videoRepository.findAll(pageable);
			page.getContent().forEach(video -> saveVideo(video, filepath));
			pageable = page.nextPageable();
		} while (page.hasNext());
	}
	private void saveVideo(YouTubeVideo video, String filepath) {
		try {
			// Define the directory path
			String directoryPath = filepath + "videos"; // Replace with actual path
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
