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
	private String userId;
	private String videoId;
	private int rating;
	private Date timestamp;

	public void generateId() {
		this.id = this.userId + "_" + this.videoId;
	}

}