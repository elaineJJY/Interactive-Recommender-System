package de.tum.rs.controller;

import de.tum.rs.dao.User;
import de.tum.rs.repository.UserRepository;
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
@RequestMapping("/login")
public class LoginController {

	@Autowired
	private UserRepository userRepository;

	@PostMapping
	public ResponseEntity<?> loginUser(@RequestBody User user) {

		userRepository.save(user);

		return ResponseEntity.ok().body("Login successful!");
	}



}
