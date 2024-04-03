package de.tum.rs.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import lombok.Builder.Default;
import lombok.Data;
import org.checkerframework.checker.optional.qual.OptionalBottom;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@Document(indexName = "users")
public class User {

	@Id
	private String userId;

	private Date feedbackLastUsed;

	private ArrayList<Double> topic_preferences; // topic preferences of the user

	// model parameters
	private HashMap<String,Integer> n_recs_per_model;

	private ArrayList<Double> recommended_topics_in_top_popular_rs;

	private Double exploit_coeff;

	private HashMap<String, ArrayList<Integer>> topic_ratings;

}
