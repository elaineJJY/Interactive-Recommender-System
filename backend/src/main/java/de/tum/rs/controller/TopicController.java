package de.tum.rs.controller;

import de.tum.rs.dao.Topic;
import de.tum.rs.dao.TopicDistribution;
import de.tum.rs.dao.User;
import de.tum.rs.dto.TopicDTO;
import de.tum.rs.repository.TopicRepository;
import de.tum.rs.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@Log4j2
@RestController
@RequestMapping("/topics")
public class TopicController {

	@Autowired
	TopicRepository topicRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RecommenderEngine recommenderEngine;

	@GetMapping()
	public ArrayList<Topic> getTopics() {
		ArrayList<Topic> topics = new ArrayList<>();
		topicRepository.findAll().forEach(topics::add);

		ArrayList<Topic> randomTopics = new ArrayList<>();
		int size = 50;
		// shuffle the topics
		for (int i = 0; i < size; i++) {
			int j = (int) (Math.random() * topics.size());
			Topic temp = topics.get(i);
			topics.set(i, topics.get(j));
			topics.set(j, temp);
			randomTopics.add(topics.get(i));
		}

		return randomTopics;
	}

	@PostMapping("/{userId}")
	public ResponseEntity<?> initializeTopics(@PathVariable String userId, @RequestBody ArrayList<Integer> topicIds) {
		try {
			log.info("Initializing topics {} for user: {}", topicIds, userId);
			recommenderEngine.regiserUser(userId, topicIds);
		}
		catch (Exception e) {
			log.error("Error while initializing topics for user: {}", userId);
			userRepository.deleteByUserId(userId);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while initializing topics for user: " + userId + " Please try again later!");
		}
		return ResponseEntity.ok().body("Topics initialized successfully for user: " + userId);
	}
}
