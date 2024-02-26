package de.tum.rs.controller;

import de.tum.rs.dao.User;
import de.tum.rs.repository.UserRepository;
import java.util.Date;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/users")
public class LoginController {

	@Autowired
	private UserRepository userRepository;

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

		if(!userRepository.findByUserId(user.getUserId()).isPresent()) {
			user.setFeedbackLastUsed(new Date());
			userRepository.save(user);
			return ResponseEntity.ok().body("Registration successful!");
		}
		log.info("User already exists!");
		return ResponseEntity.badRequest().body("User already exists!");
	}



}
