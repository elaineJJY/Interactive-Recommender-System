package de.tum.rs.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@Document(indexName = "users")
public class User {

	@Id
	private String userId;

	private Date feedbackLastUsed;

	private ArrayList<Double> topic_preferences; // topic preferences of the user

	// model parameters
	private HashMap<String,Integer> n_recs_per_model;

	private HashMap<String,ArrayList<Integer>> topic_categories;

	private ArrayList<Integer> recommended_topics_in_top_popular_rs;

	private Double exploit_coeff;

	private HashMap<String, ArrayList<Integer>> topic_ratings;

	private HashMap<Integer,Double> processed_topic_scores; // <topic_id, percentage_score>

	private ArrayList<String> disliked_creators;

	private ArrayList<String> disliked_creators_video_ids;

	// for user study
	private Date registrationDate;

	private Answers answers;

	@Data
	public static class Answers {
		// Social media usage
		private String socialMediaFrequency;  // Frequency of social media usage
		private String recommenderSystemFamiliarity;  // Familiarity with recommender systems
		private HashMap<String, String> dsiResponses;  // Responses to DSI questions

		// Technical knowledge
		private String videoRecommendationBasis;  // Basis of video recommendations
		private List<String> methodsToInfluenceRS;  // Methods to influence the recommender system
	}
}
