package de.tum.rs.controller;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.youtube.model.Video;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.model.Recommendation;
import de.tum.rs.repository.VideoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.util.concurrent.ListenableFuture;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFutureAdapter;
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

	@Autowired
	RecommenderEngine recommenderEngine;

	private Random random = new Random();


	@GetMapping("/recommendations")
	@Operation(summary = "Get recommendations using model", description = "Returns recommendations stored in the database.")
	@Async
	public CompletableFuture<List<Recommendation>> getRecommendations(@Nullable String userId) {
		return CompletableFuture.supplyAsync(() -> {
			log.info("Getting recommendations for user {}", userId);
			List<Recommendation> recommendations;
			try {
				recommendations = recommenderEngine.getRecommendations(userId);
				recommendations.forEach(recommendation -> {
					recommendation.setVideo(
						videoRepository.findById(recommendation.getVideoId()).get());
				});
			} catch (Exception e) {
				int page = random.nextInt(500);
				Page<YouTubeVideo> videoPage = videoRepository.findAll(PageRequest.of(page, 20));
				List<YouTubeVideo> videos = videoPage.getContent();
				recommendations = Recommendation.from(videos, "Random generated recommendations");
			}
			log.info("Got {} recommendations", recommendations.size());
			return recommendations;
		});
	}

	@GetMapping("/search")
	@Operation(summary = "Search videos from elastic search", description = "Returns videos stored in the database.")
	@Async
	public CompletableFuture<List<Recommendation>> searchVideos(
		@Parameter(example = "shorts") @RequestParam String keyword,
		@Parameter(example = "0") @RequestParam int page) {
		return CompletableFuture.supplyAsync(() -> {
			List<YouTubeVideo> videos = videoRepository.findByKeyword(keyword,
					PageRequest.of(page, 20))
				.getContent();
			List<Recommendation> recommendations = Recommendation.from(videos, "Search results");
			log.info("Got {} search results for keyword {}", recommendations.size(), keyword);
			return recommendations;
		});
	}





}
