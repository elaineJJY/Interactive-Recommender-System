package de.tum.rs.dao;


import com.google.api.client.util.DateTime;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "feedback")
public class Feedback {
	private String userId;
	private String videoId;
	private int rating;
	private DateTime timestamp;
}