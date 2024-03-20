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

	// Pablo defines
	private HashMap<String, String> models;

	private ArrayList<TopicDTO> topic_preferences; // topic preferences of the user

}
