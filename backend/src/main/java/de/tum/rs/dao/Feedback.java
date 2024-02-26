package de.tum.rs.dao;


import co.elastic.clients.util.DateTime;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "feedback")
public class Feedback {

	@Id
	private String id;
	private Date timestamp;
	private String userId;
	private String videoId;

	// user click-through history
	private int rating; // valid rating:[0,5], -1 means not rated
	private float totalWatchTime;  // total watch time in seconds
	private LinkedList<Interaction> interactions;

	public void generateId() {
		this.id = this.userId + "_" + this.videoId;
	}

	@Data
	static class Interaction {
		private String type;
		private float time; // watched time in seconds
	}
}