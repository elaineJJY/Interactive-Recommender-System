package de.tum.rs.model;

import de.tum.rs.dao.YouTubeVideo;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import reactor.util.annotation.Nullable;

@Data
public class Recommendation {
	private String videoId;

	@Nullable
	private YouTubeVideo video;

	private String explanation;

	public static List<Recommendation> from(List<YouTubeVideo> videos, String explanation) {
		List<Recommendation> recommendations = new LinkedList<>();
		for (YouTubeVideo video : videos) {
			recommendations.add(Recommendation.of(video, explanation));
		}
		return recommendations;
	}
	public static Recommendation of(YouTubeVideo video, String explanation) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideo(video);
		recommendation.setVideoId(video.getId());
		recommendation.setExplanation(explanation);
		return recommendation;
	}
}
