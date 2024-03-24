package de.tum.rs.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.tum.rs.dao.Feedback;
import de.tum.rs.model.Recommendation;
import de.tum.rs.repository.VideoRepository;
import de.tum.rs.util.RecommendationBuilder;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
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
	private VideoRepository videoRepository;

	ObjectMapper mapper = new ObjectMapper();

	public void regiserUser(String userId) {
		String url = PYTHON_SERVICE_URL + "/register?userId=" + userId;
		restTemplate.postForObject(url, null, Void.class);
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
	 * @param feedbacks
	 */
	public void invokeUpdate(List<Feedback> feedbacks) {
		log.info("Invoke model update with {} feedbacks", feedbacks.size());
		String url = PYTHON_SERVICE_URL + "/feedback";
		restTemplate.postForObject(url, feedbacks, Void.class);
	}
}
