package de.tum.rs.controller;

import de.tum.rs.dao.Topic;
import de.tum.rs.dao.User;
import de.tum.rs.dto.TopicDTO;
import de.tum.rs.dto.UserDTO;
import de.tum.rs.repository.TopicRepository;
import de.tum.rs.repository.UserRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private RecommenderEngine recommenderEngine;

	@PostMapping
	@RequestMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		log.info("User: {}", user);
		if(userRepository.findByUserId(user.getUserId()).isPresent()) {
			return ResponseEntity.ok().body("Login successful!");
		}
		log.info("User does not exist!");
		return ResponseEntity.badRequest().body("User does not exist!");
	}

	@PostMapping
	@RequestMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {

		if( user.getUserId() == null || user.getUserId().equals("")) {
			log.info("User ID cannot be empty!");
			return ResponseEntity.badRequest().body("User ID cannot be empty!");
		}

		// check if user id only contains alphanumeric characters
		if (!user.getUserId().matches("^[a-zA-Z0-9]*$")) {
			log.info("User ID can only contain alphanumeric characters!");
			return ResponseEntity.badRequest().body("User ID can only contain alphanumeric characters!");
		}

		if ( user.getUserId().equals("null") ) {
			log.info("User ID cannot be null!");
			return ResponseEntity.badRequest().body("User ID cannot be null!");
		}

		if(!userRepository.findByUserId(user.getUserId()).isPresent()) {
			user.setFeedbackLastUsed(new Date());
			user.setRegistrationDate(new Date());
			userRepository.save(user);
			log.info("User {} registered successfully!", user.getUserId());
			return ResponseEntity.ok().body("Registration successful!");
		}
		log.info("User already exists!");
		return ResponseEntity.badRequest().body("User already exists!");
	}

	@GetMapping("/{userId}")
	public ResponseEntity<?> getUser(@PathVariable String userId) {
		userId = userId.substring(1, userId.length() - 1);
		if(userRepository.findByUserId(userId).isPresent()) {
			recommenderEngine.invokeProcessFeedback(userId);
			log.info("Loading User profile from database for user {}", userId);
			User user = userRepository.findByUserId(userId).get();
			UserDTO userDTO = new UserDTO();
			userDTO.setUserId(user.getUserId());
			userDTO.setExploit_coeff(user.getExploit_coeff());
			userDTO.setN_recs_per_model(user.getN_recs_per_model());

			// find top 10 topics
			ArrayList<TopicDTO> top10TopicDto = new ArrayList<>();
			user.getProcessed_topic_scores().forEach((topicId, score) -> {
				Topic topic = topicRepository.findById(topicId).get();
				TopicDTO topicDTO = new TopicDTO(score, topic);
				top10TopicDto.add(topicDTO);
			});


			userDTO.setTopic_preferences(top10TopicDto);
			log.info("User {} retrieved successfully!", userId);
			return ResponseEntity.ok().body(userDTO);
		}
		log.info("User {} does not exist!", userId);
		return ResponseEntity.badRequest().body("User does not exist!");
	}

	@PostMapping("/{userId}")
	public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO) {
		userId = userId.substring(1, userId.length() - 1);
		if(userRepository.findByUserId(userId).isPresent()) {
			User user = userRepository.findByUserId(userId).get();
			user.setExploit_coeff(userDTO.getExploit_coeff());
			user.setN_recs_per_model(userDTO.getN_recs_per_model());

			ArrayList<TopicDTO> topicDTOs = userDTO.getTopic_preferences();
			HashMap<Integer, Double> top10Topics = new HashMap<>();
			for (TopicDTO topicDTO : topicDTOs) {
				top10Topics.put(topicDTO.getId(), topicDTO.getScore());
			}
			user.setProcessed_topic_scores(top10Topics);
			userRepository.save(user);
			try {
				recommenderEngine.invokeUpdateModel(userId);
			} catch (Exception e) {
				log.error("Error while invoking model update", e);
			}
			log.info("User {} updated successfully!", userId);
			return ResponseEntity.ok().body("User updated successfully!");
		}
		log.info("User does not exist!");
		return ResponseEntity.badRequest().body("User does not exist!");
	}



}
