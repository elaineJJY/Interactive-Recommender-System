package de.tum.rs.controller;

import de.tum.rs.dao.User;
import de.tum.rs.repository.UserRepository;
import java.util.List;
import de.tum.rs.dao.Feedback;
import de.tum.rs.repository.FeedbackRepository;
import java.util.concurrent.CompletableFuture;
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
	public CompletableFuture<Void> saveFeedbacks(@RequestBody List<Feedback> feedbacks) {

		CompletableFuture.runAsync(() -> {
			feedbacks.forEach(feedback -> {
				feedback.generateId();
				feedbackRepository.save(feedback);
			});
			log.info("Saved feedbacks: {}", feedbacks);
		});
		return CompletableFuture.completedFuture(null);
	}
}
