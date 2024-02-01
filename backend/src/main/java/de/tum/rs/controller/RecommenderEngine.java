package de.tum.rs.controller;


import de.tum.rs.dao.Feedback;
import de.tum.rs.model.Recommendation;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RecommenderEngine {
	public List<Recommendation> getRecommendations(String userId) {
		// call localhost:8089/recommendations/{userId} [model]
		// get List<videoId,explanation> as response
		throw new UnsupportedOperationException();
	}

	public void saveFeedback(List<Feedback> feedbacks) {
		// call localhost:8089/feedback [model]
		// send List<Feedback> as request
		throw new UnsupportedOperationException();
	}
}
