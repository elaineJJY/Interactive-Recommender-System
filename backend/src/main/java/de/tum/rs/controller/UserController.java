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
			userDTO.setModels(user.getModels());
			ArrayList<Double> scores = user.getTopic_preferences();
			ArrayList<TopicDTO> topicDTOs = new ArrayList<>();
			for (int i = 0; i < scores.size(); i++) {
				Topic topic = topicRepository.findById(i).get();
				TopicDTO topicDTO = new TopicDTO(scores.get(i), topic);
				topicDTOs.add(topicDTO);
			}
			userDTO.setTopic_preferences(topicDTOs);
			return ResponseEntity.ok().body(userDTO);
		}
		log.info("User does not exist!");
		return ResponseEntity.badRequest().body("User does not exist!");
	}



}
