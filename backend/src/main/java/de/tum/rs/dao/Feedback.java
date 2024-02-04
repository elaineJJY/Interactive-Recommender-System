package de.tum.rs.dao;


import co.elastic.clients.util.DateTime;
import java.time.Instant;
import java.util.Date;
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
	private int rating;
	private String totalWatchTime;



	public void generateId() {
		this.id = this.userId + "_" + this.videoId;
	}

}