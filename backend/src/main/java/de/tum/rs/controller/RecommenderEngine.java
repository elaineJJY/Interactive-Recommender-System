package de.tum.rs.controller;


import de.tum.rs.model.Recommendation;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RecommenderEngine {
	public List<Recommendation> getRecommendations(String userId) {
		throw new UnsupportedOperationException();
	}
}
