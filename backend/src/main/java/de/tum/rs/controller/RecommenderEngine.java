package de.tum.rs.controller;


import de.tum.rs.dao.Feedback;
import de.tum.rs.model.Recommendation;
import de.tum.rs.repository.VideoRepository;
import de.tum.rs.util.RecommendationBuilder;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Log4j2
@Service
public class RecommenderEngine {

	@Value("${python.rs.service.url}")
	private String PYTHON_SERVICE_URL;
	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private VideoRepository videoRepository;

//	public List<Recommendation> getDummyRecommendations() {
//
//		String url = PYTHON_SERVICE_URL + "/recommendations?num=" + 10;
//
//
//		log.info("Getting recommendations from python model: {}", url);
//		List<String> response = restTemplate.getForObject(url, List.class);
//		log.info("Response: {}", response);
//		List<Recommendation> recommendations = new LinkedList<>();
//		for (String videoId : response) {
//			log.info("VideoId: {}", videoId);
//			Recommendation recommendation = new Recommendation();
//			recommendation.setVideoId(videoId);
//			recommendation.setExplanation("Dummy recommendation");
//			recommendation.setVideo(videoRepository.findById(videoId).get());
//			recommendations.add(recommendation);
//		}
//
//
////		List<Recommendation> recommendations = restTemplate.getForObject(url, List.class);
////		recommendations.forEach(recommendation -> {
////			log.info("Recommendation: {}", recommendation);
////			recommendation.setVideo(videoRepository.findById(recommendation.getVideoId()).get());
////		});
//
//		return recommendations;
//	}

	/**
	 * Get recommendations for the given user
	 * @param userId
	 * @return list of recommendations
	 */
	public List<Recommendation> getRecommendations(String userId) {

		String url = PYTHON_SERVICE_URL + "/recommendations?userId=" + userId;

		List<Recommendation> recommendations = restTemplate.getForObject(url, List.class);
		recommendations.forEach(recommendation -> {
			log.info("Recommendation: {}", recommendation);
			recommendation.setVideo(videoRepository.findById(recommendation.getVideoId()).get());
		});
		return recommendations;
	}


	/**
	 * Invoke the model update with the given feedbacks
	 * (this function is called from the FeedbackController.saveFeedbacks() method when the user has given more than 5 unused feedbacks)
	 * @param feedbacks
	 */
	public void invokeUpdate(List<Feedback> feedbacks) {
		log.info("Invoke model update with {} feedbacks: {}", feedbacks.size(), feedbacks);
		String url = PYTHON_SERVICE_URL + "/feedback";
		restTemplate.postForObject(url, feedbacks, Void.class);
	}
}