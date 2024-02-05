package de.tum.rs.util;

import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.model.Recommendation;
import de.tum.rs.repository.VideoRepository;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class RecommendationBuilder {
	@Autowired
	private static VideoRepository videoRepository;

	public static Recommendation build(YouTubeVideo video) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideo(video);
		recommendation.setVideoId(video.getId());
		return recommendation;
	}

	public static Recommendation build(String videoId, String explanation) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideoId(videoId);
		recommendation.setExplanation(explanation);
		recommendation.setVideo(videoRepository.findById(videoId).get());
		return recommendation;
	}
	public static Recommendation build(YouTubeVideo video, String explanation) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideo(video);
		recommendation.setVideoId(video.getId());
		recommendation.setExplanation(explanation);
		return recommendation;
	}

//	public static List<Recommendation> buildList(List<String> videos, String explanation) {
//		List<Recommendation> recommendations = new LinkedList<>();
//		for (String videoId : videos) {
//			recommendations.add(RecommendationBuilder.build(videoId, "Dummy recommendation"));
//		}
//		return recommendations;
//	}

	public static List<Recommendation> buildList(List<YouTubeVideo> videos, String explanation) {
		List<Recommendation> recommendations = new LinkedList<>();
		for (YouTubeVideo video : videos) {
			recommendations.add(RecommendationBuilder.build(video, explanation));
		}
		return recommendations;
	}


}
