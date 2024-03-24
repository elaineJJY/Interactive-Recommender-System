package de.tum.rs.dao;

import java.util.LinkedHashMap;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "topics")
public class Topic {

	@Id
	private Integer topicNumber;

	private String description;
	private Long document_count;

	@Field(type = FieldType.Object)
	private LinkedHashMap<Object,Object> tokens;

	public Topic() {
	}
	public Topic(Integer topicNumber, String description, Long document_count, LinkedHashMap<Object,Object> tokens) {
		this.topicNumber = topicNumber;
		this.description = description;
		this.document_count = document_count;
		this.tokens = tokens;
	}
}
