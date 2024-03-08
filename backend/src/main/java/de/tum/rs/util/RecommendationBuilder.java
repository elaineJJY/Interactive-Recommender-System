package de.tum.rs.util;

import de.tum.rs.dao.Topic;
import de.tum.rs.dao.TopicDistribution;
import de.tum.rs.dao.TopicDistribution.TopicScore;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.model.Recommendation;
import de.tum.rs.model.Recommendation.TopicDTO;
import de.tum.rs.repository.TopicDistributionRepository;
import de.tum.rs.repository.TopicRepository;
import de.tum.rs.repository.VideoRepository;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RecommendationBuilder {

	private Random random = new Random();

	@Autowired
	VideoRepository videoRepository;

	@Autowired
	TopicRepository topicRepository;
	@Autowired
	TopicDistributionRepository topicDistributionRepository;

	public Recommendation build(YouTubeVideo video) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideo(video);
		recommendation.setVideoId(video.getId());
		return recommendation;
	}

	public Recommendation build(String videoId, String explanation) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideoId(videoId);
		recommendation.setExplanation(explanation);
		this.fillBlank(recommendation);
		return recommendation;
	}
	public Recommendation build(YouTubeVideo video, String explanation) {
		Recommendation recommendation = new Recommendation();
		recommendation.setVideoId(video.getId());
		recommendation.setExplanation(explanation);
		this.fillBlank(recommendation);
		return recommendation;
	}

//	public static List<Recommendation> buildList(List<String> videos, String explanation) {
//		List<Recommendation> recommendations = new LinkedList<>();
//		for (String videoId : videos) {
//			recommendations.add(RecommendationBuilder.build(videoId, "Dummy recommendation"));
//		}
//		return recommendations;
//	}

	public List<Recommendation> buildList(List<YouTubeVideo> videos, String explanation) {
		List<Recommendation> recommendations = new LinkedList<>();
		for (YouTubeVideo video : videos) {
			recommendations.add(this.build(video, explanation));
		}
		return recommendations;
	}

	public Recommendation fillBlank(Recommendation recommendation) {
		addVideoDetail(recommendation);
		addTopics(recommendation);
		return recommendation;
	}

	public Recommendation addVideoDetail(Recommendation recommendation) {
		recommendation.setVideo(videoRepository.findById(recommendation.getVideoId()).get());
		return recommendation;
	}

	public Recommendation addTopics(Recommendation recommendation) {
		Optional<TopicDistribution> topicDistribution = topicDistributionRepository.findById(recommendation.getVideoId());

		if (topicDistribution.isPresent()) {
			List<TopicDTO> topics = new LinkedList<>();
			for (TopicDistribution.TopicScore topicScore : topicDistribution.get().getMost_relevant_topics()) {
				Topic topic = topicRepository.findById(topicScore.getTopic_index()).get();
				topics.add(new TopicDTO(topicScore.getScore(), topic));
			}
			recommendation.setTopics(topics);
		}

		return recommendation;
	}

	public List<Recommendation> getRandomRecommendations() {
		List<Recommendation> recommendations = new LinkedList<>();
		int page = random.nextInt(500);
		List<TopicDistribution> topicDistributions = topicDistributionRepository.findAll(
			PageRequest.of(page, 10)).getContent();
		for (TopicDistribution topicDistribution : topicDistributions) {
			Recommendation recommendation = new Recommendation();
			recommendation.setVideoId(topicDistribution.getVideoId());
			recommendation.setExplanation("Random recommendation");
			this.fillBlank(recommendation);
			recommendations.add(recommendation);
		}
		return recommendations;
	}
}
