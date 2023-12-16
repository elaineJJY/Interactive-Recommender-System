package de.tum.rs.controller;

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
	private ElasticsearchOperations elasticsearchOperations;

	private static final String INDEX_PREFIX = "profile.";

	@PostMapping
	public ResponseEntity<?> loginUser(@RequestBody String userId) {

		String indexName = INDEX_PREFIX + userId;

		// Check if the index exists
		boolean indexExists = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();

		if (!indexExists) {
			// If the index doesn't exist, create it
			elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).create();
			log.info("Created index: " + indexName);
		}

		return ResponseEntity.ok().body("Login successful!");
	}



}
