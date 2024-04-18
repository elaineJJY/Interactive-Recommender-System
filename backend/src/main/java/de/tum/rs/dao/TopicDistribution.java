package de.tum.rs.dao;

import java.util.LinkedHashMap;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import org.elasticsearch.core.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "topic_distributions_test")
public class TopicDistribution {

	@Id
	private String videoId;

	private List<TopicScore> most_relevant_topics;

	@Data
	public static class TopicScore {
		Integer topic_index;
		Double score;
	}

}
