package de.tum.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.repository.VideoRepository;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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

	@GetMapping("/elasticsearch")
	public void checkElasticsearch() {
		try {
			String response = new RestTemplate().getForObject("http://es:9200/", String.class);
			log.info("Received response from Elasticsearch: {}", response);
		} catch (Exception e) {
			log.error("Failed to connect to Elasticsearch at localhost:9200", e);
		}
	}

	@PostMapping("/transferVideos")
	public void transferData(@Parameter(example = "/Users/elaine/Downloads/videos") @RequestParam String folderPath) {
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


}
