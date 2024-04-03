package de.tum.rs.controller;

import de.tum.rs.dao.Topic;
import de.tum.rs.dao.User;
import de.tum.rs.dto.TopicDTO;
import de.tum.rs.dto.UserDTO;
import de.tum.rs.repository.TopicDistributionRepository;
import de.tum.rs.repository.TopicRepository;
import de.tum.rs.repository.UserRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
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

		if(user.getUserId().equals("") || user.getUserId() == null) {
			log.info("User ID cannot be empty!");
			return ResponseEntity.badRequest().body("User ID cannot be empty!");
		}

		if(!userRepository.findByUserId(user.getUserId()).isPresent()) {
			user.setFeedbackLastUsed(new Date());
			userRepository.save(user);
			return ResponseEntity.ok().body("Registration successful!");
		}
		log.info("User already exists!");
		return ResponseEntity.badRequest().body("User already exists!");
	}

	@GetMapping("/{userId}")
	public ResponseEntity<?> getUser(@PathVariable String userId) {
		if(userRepository.findByUserId(userId).isPresent()) {
			User user = userRepository.findByUserId(userId).get();
			UserDTO userDTO = new UserDTO();
			userDTO.setUserId(user.getUserId());
			userDTO.setExploit_coeff(user.getExploit_coeff());
			userDTO.setN_recs_per_model(user.getN_recs_per_model());
			ArrayList<Double> scores = user.getTopic_preferences();
			PriorityQueue<TopicDTO> topicDTOs = new PriorityQueue<>((a, b) -> Double.compare(b.getScore(), a.getScore()));
			for (int i = 0; i < scores.size(); i++) {
				Topic topic = topicRepository.findById(i).get();
				TopicDTO topicDTO = new TopicDTO(scores.get(i), topic);
				topicDTOs.add(topicDTO);
			}

			// find top 11 topics from topic DTOs
			ArrayList<TopicDTO> top11 = new ArrayList<>();
			Double score = 0.0;
			for (int i = 0; i < 11; i++) {
				TopicDTO topicDTO = topicDTOs.poll();
				if (topicDTO != null) {
					score += topicDTO.getScore();
					top11.add(topicDTO);
				}
			}

			// create a new topic DTO for "others"
			Topic others = new Topic();
			others.setTopicNumber(-1);
			others.setDescription("Others");
			TopicDTO othersDTO = new TopicDTO(100-score, others);
			userDTO.setOrigin_other_topics(othersDTO);

			top11.add(othersDTO);
			userDTO.setTopic_preferences(top11);
			log.info("User {} retrieved successfully!", userId);
			return ResponseEntity.ok().body(userDTO);
		}
		log.info("User does not exist!");
		return ResponseEntity.badRequest().body("User does not exist!");
	}

	@PostMapping("/{userId}")
	public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO) {
		if(userRepository.findByUserId(userId).isPresent()) {
			User user = userRepository.findByUserId(userId).get();

			ArrayList<TopicDTO> topicDTOs = userDTO.getTopic_preferences();
			double ratio = topicDTOs.get(topicDTOs.size()-1).getScore() / userDTO.getOrigin_other_topics().getScore();
			ArrayList<Double> topic_preferences = new ArrayList<>();
			ArrayList<Double> origin_topic_preferences = user.getTopic_preferences();
			for(int i = 0; i < origin_topic_preferences.size(); i++) {
				topic_preferences.add(origin_topic_preferences.get(i) * ratio);
			}
			for (TopicDTO topicDTO : topicDTOs) {
				if(topicDTO.getId() != -1 ) {
					topic_preferences.set(topicDTO.getId(), topicDTO.getScore());
				}
			}
			user.setTopic_preferences(topic_preferences);
			user.setExploit_coeff(userDTO.getExploit_coeff());
			user.setN_recs_per_model(userDTO.getN_recs_per_model());
			userRepository.save(user);
			try {
				recommenderEngine.invokeUpdateTopicRating(userId);
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
