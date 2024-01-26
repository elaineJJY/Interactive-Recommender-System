package de.tum.rs.controller;

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

	@PostMapping
	@Async
	public void saveFeedback(@RequestBody List<Feedback> feedbacks) {

		feedbacks.stream().forEach(feedback -> {
			feedback.generateId();
			feedbackRepository.save(feedback);
		});
		log.info("Saving feedbacks: {}", feedbacks);
	}
}
