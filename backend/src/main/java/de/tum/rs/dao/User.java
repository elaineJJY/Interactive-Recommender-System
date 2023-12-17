package de.tum.rs.dao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@Document(indexName = "users")
public class User {

	@Id
	private String userId;

}
