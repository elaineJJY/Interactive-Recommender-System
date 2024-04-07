package de.tum.rs.controller;


import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.dto.Recommendation;
import de.tum.rs.repository.VideoRepository;
import de.tum.rs.util.RecommendationBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
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

	@Autowired
	private RecommendationBuilder recommendationBuilder;


	@GetMapping("/recommendations")
	@Operation(summary = "Get recommendations using model", description = "Returns recommendations stored in the database.")
	@Async
	public CompletableFuture<List<Recommendation>> getRecommendations(@Nullable String userId) {
		return CompletableFuture.supplyAsync(() -> {
			List<Recommendation> recommendations;

			try {
				if(userId == null || userId.isEmpty() || userId.equals("null")) {
					log.info("Getting recommendations for user {}", userId);
					throw new IllegalArgumentException("Not logged in");
				}
				recommenderEngine.invokeProcessFeedback(userId);
				recommendations = recommenderEngine.getRecommendations(userId);
				recommendations.forEach(recommendation -> {
					recommendation.setVideo(
						videoRepository.findById(recommendation.getVideoId()).get());
				});
			}
			catch (IllegalArgumentException e) {
				log.error("User not logged in", e);
				log.info("Falling back to random recommendations");
				recommendations = recommendationBuilder.getRandomRecommendations();
			}
			catch (Exception e) {
				log.error("Error getting recommendations from Python model", e);
				log.info("Falling back to random recommendations");
				recommendations = recommendationBuilder.getRandomRecommendations();
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
			List<Recommendation> recommendations = recommendationBuilder.buildList(videos, "Search results");
			log.info("Got {} search results for keyword {}", recommendations.size(), keyword);
			return recommendations;
		});
	}


}
