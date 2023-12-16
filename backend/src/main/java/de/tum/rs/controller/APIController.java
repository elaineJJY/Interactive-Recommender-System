package de.tum.rs.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api-key")
public class APIController {
	private List<String> apiKeys = new ArrayList<>();
	private int currentKeyIndex = 0;

	public APIController(){
		// load apikeys from environment variables
		String apiKeyString = System.getenv("API_KEYS");

		log.info("API keys: {}", apiKeyString);
		if (apiKeyString != null) {
			String[] apiKeyArray = apiKeyString.split("\\,");
			for (String apiKey : apiKeyArray) {
				String trimmedApiKey = apiKey.trim();
				if (!trimmedApiKey.isEmpty()) {
					apiKeys.add(trimmedApiKey);
				}
			}
		}
	}

	@GetMapping("/list")
	@Operation(summary = "Get list of API keys", description = "Returns the list of API keys.")
	public List<String> getApiKeys() {
		return apiKeys;
	}

	@PostMapping
	public void addApiKey(String apiKey) {
		apiKeys.add(apiKey);
	}


	@GetMapping("/current")
	public String getCurrentApiKey() {
		if (apiKeys == null || apiKeys.isEmpty()) {
			log.error("No API keys available");
			return null;
		}
		String apiKey = apiKeys.get(currentKeyIndex);
		return apiKey;
	}

	@GetMapping("/next")
	@Operation(summary = "Switch to next API key", description = "Switches to the next API key in the list of API keys.")
	public void switchToNextKey() {
		currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
	}


}
