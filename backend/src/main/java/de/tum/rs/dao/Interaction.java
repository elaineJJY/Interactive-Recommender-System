package de.tum.rs.dao;


import java.util.Date;
import lombok.Data;
import org.elasticsearch.core.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "interaction")
public class Interaction {

	@Id
	private String id;

	private String userId;

	private String page;

	private Date timestamp;

	@Nullable
	private String videoId;

	private String component;

	@Nullable
	private String componentValue;

	public Interaction() {
	}
}
