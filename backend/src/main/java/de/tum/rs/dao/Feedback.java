package de.tum.rs.dao;


import co.elastic.clients.util.DateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import lombok.Data;
import org.elasticsearch.core.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Feedback
 * Each feedback is a record of a user's interaction with a video
 * It will be stored in the feedback index when the user watches a video more than 1 seconds
 */
@Data
@Document(indexName = "feedback")
public class Feedback {

	@Id
	private String id;
	private Date timestamp;
	private String userId;
	private String videoId;

	// user click-through history
	private int rating; // valid rating:[0,5], 0 means not rated, 1-5 means rated and bigger number means better
	private float totalWatchTime;  // total watch time in seconds
	private LinkedList<Interaction> interactions;



	@Nullable
	private ArrayList<String> more;	// topic groups that the user is interested in

	@Nullable
	private ArrayList<String> less; // topic groups that the user is not interested in

	@Nullable
	private ArrayList<String> dislikeReasons;

	public void generateId() {
		this.id = this.userId + "_" + this.videoId;
	}

	@Data
	static class Interaction {
		private String type;
		private float time; // watched time in seconds
	}
}