package de.tum.rs.controller;

import de.tum.rs.dao.User;
import de.tum.rs.repository.UserRepository;
import java.util.List;
import de.tum.rs.dao.Feedback;
import de.tum.rs.repository.FeedbackRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

	@Autowired
	private FeedbackRepository feedbackRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RecommenderEngine recommenderEngine;

	@PostMapping
	@Async
	public void saveFeedbacks(@RequestBody List<Feedback> feedbacks) {

		feedbacks.stream().forEach(feedback -> {
			feedback.generateId();
			feedbackRepository.save(feedback);
		});
		log.info("Saving feedbacks: {}", feedbacks);

		// check if the user has given enough feedbacks to update the model
		User user = userRepository.findById(feedbacks.get(0).getUserId()).get();
		feedbacks = feedbackRepository.findByUserIdAndTimestampGreaterThan(user.getUserId(), user.getFeedbackLastUsed());
		if(feedbacks.size() >= 5) {
			log.info("Invoking model update for User {}", user.getUserId());
			try {
				recommenderEngine.invokeUpdate(feedbacks);
				user.setFeedbackLastUsed(feedbacks.get(feedbacks.size() - 1).getTimestamp());
				userRepository.save(user);
			} catch (Exception e) {
				log.error("Error while invoking model update", e);
			}
		}
	}
}
