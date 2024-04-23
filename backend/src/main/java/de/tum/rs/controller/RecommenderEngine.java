package de.tum.rs.controller;


import de.tum.rs.dao.Feedback;
import de.tum.rs.dao.User;
import de.tum.rs.dto.Recommendation;
import de.tum.rs.repository.FeedbackRepository;
import de.tum.rs.repository.UserRepository;
import de.tum.rs.util.RecommendationBuilder;
import java.util.LinkedHashMap;
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
	private RecommendationBuilder recommendationBuilder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FeedbackRepository feedbackRepository;

	/**
	 * Register the user with the given topics
	 * @param userId
	 * @param topicIds
	 */
	public void regiserUser(String userId, List<Integer> topicIds) throws InterruptedException {
		String url = PYTHON_SERVICE_URL + "/register?userId=" + userId;
		String response = restTemplate.postForObject(url, topicIds, String.class);
//		Thread.sleep(500);
		log.info("Called Model to register user {} with topics {}", userId, topicIds);
		log.info("From Model: {}", response);
	}

	/**
	 * Get recommendations for the given user
	 * @param userId
	 * @return list of recommendations
	 */
	public List<Recommendation> getRecommendations(String userId) {

		String url = PYTHON_SERVICE_URL + "/recommendations?userId=" + userId;

		// serialize the response to a list of recommendations
		List<LinkedHashMap<String, String>>response = restTemplate.getForObject(url, List.class);
		List<Recommendation> recommendations = new LinkedList<>();
		response.forEach(json -> {
			Recommendation recommendation = new Recommendation();
			recommendation.setVideoId(json.get("videoId"));
			recommendation.setExplanation(json.get("explanation"));
			recommendationBuilder.fillBlank(recommendation);
			recommendations.add(recommendation);
		});

		log.info("Got {} recommendations for user {} using Model", recommendations.size(), userId);
		return recommendations;
	}


	/**
	 * Invoke the model update with the given feedbacks
	 * (this function is called from the FeedbackController.saveFeedbacks() method when the user has given more than 5 unused feedbacks)
	 * @param userId
	 */
	public void invokeProcessFeedback(String userId) {
		String url = PYTHON_SERVICE_URL + "/feedback";
		try {
			User user = userRepository.findByUserId(userId).get();
			List<Feedback> recentFeedbacks = feedbackRepository.findByUserIdAndTimestampGreaterThan(
				user.getUserId(), user.getFeedbackLastUsed()
			);
			log.info("Invoking model update for User {} with {} Feedbacks", userId, recentFeedbacks.size());
			String response = restTemplate.postForObject(url, recentFeedbacks, String.class);
			//		Thread.sleep(500);
			log.info("From Model: {}", response);
		} catch (Exception e) {
			log.error("Error while invoking model update", e);
		}
	}

	/**
	 * Invoke the model updateing the topic rating
	 * (this function is called from the TopicController.updateUser() when the user edit the topic rating)
	 * @param userId
	 */
	public void invokeUpdateModel(String userId) {
		String url = PYTHON_SERVICE_URL + "/model";
		restTemplate.postForObject(url, userId, Void.class);
		log.info("Invoked model updating the topic rating", userId);
	}
}
