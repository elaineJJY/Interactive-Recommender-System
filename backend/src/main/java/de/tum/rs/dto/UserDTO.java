package de.tum.rs.dto;

import de.tum.rs.dao.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class UserDTO {
	@Id
	private String userId;

	private HashMap<String,Integer> n_recs_per_model;

	// Top-10 most relevant topics and their scores
	private ArrayList<TopicDTO> topic_preferences; // topic preferences of the user
	private Double exploit_coeff;
}
