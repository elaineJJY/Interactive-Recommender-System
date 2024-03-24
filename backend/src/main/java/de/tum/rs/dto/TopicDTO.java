package de.tum.rs.dto;

import de.tum.rs.dao.Topic;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class TopicDTO {
	private Integer id;
	Double score;
	private String description;

	public TopicDTO() {
	}

	public TopicDTO(Double score, Topic topic) {
		this.score = score;
		this.id = topic.getTopicNumber();
		this.description = topic.getDescription();
	}
}
