package de.tum.rs.model;

import de.tum.rs.dao.Topic;
import de.tum.rs.dao.TopicDistribution.TopicScore;
import de.tum.rs.dao.YouTubeVideo;
import de.tum.rs.dto.TopicDTO;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import reactor.util.annotation.Nullable;

@Data
public class Recommendation {
	private String videoId;
	private String explanation;

	@Nullable
	private YouTubeVideo video;

	@Nullable
	// most relevant topics and their scores
	private List<TopicDTO> topics;



}
