package de.tum.rs.controller;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;


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
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	public List<YouTubeVideo> getRecommendations(@Nullable String userId) {
		int page = random.nextInt(500);
		Page<YouTubeVideo> videoPage = videoRepository.findAll(PageRequest.of(page, 20));
		List<YouTubeVideo> videos = videoPage.getContent();
		log.info("Got {} recommendations", videos.size());
		return videos;
	}

	@GetMapping("/search")
	@Operation(summary = "Search videos from elastic search", description = "Returns videos stored in the database.")
	public List<YouTubeVideo> searchVideos(
		@Parameter(example = "shorts") @RequestParam String keyword,
		@Parameter(example = "0") @RequestParam int page) {
		List<YouTubeVideo> videos = videoRepository.findByKeyword(keyword, PageRequest.of(page, 20))
			.getContent();

		log.info("Found {} videos with keyword {}", videos.size(), keyword);
		return videos;
	}


	@GetMapping("/all")
	@Operation(summary = "Get videos from elastic search in pages", description = "Returns all videos stored in the database.")
	public List<YouTubeVideo> getAllVideos(int page, int size) {
		List<YouTubeVideo> videos = videoRepository.findAll(PageRequest.of(page, size))
			.getContent();

		log.info("Found {} videos", videos.size());
		return videos;
	}



}
