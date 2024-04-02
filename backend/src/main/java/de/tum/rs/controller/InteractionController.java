package de.tum.rs.controller;

import de.tum.rs.dao.Interaction;
import de.tum.rs.repository.InteractionRepository;
import java.util.Date;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Log4j2
@Controller
@RequestMapping("/interactions")
public class InteractionController {

	@Autowired
	private InteractionRepository interactionRepository;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void saveInteractions(@RequestBody List<Interaction> interactions) {
		log.info("Saving interactions: {}", interactions);
		interactionRepository.saveAll(interactions);
	}
}
