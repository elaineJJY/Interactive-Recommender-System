package de.tum.rs.model;

import de.tum.rs.dao.YouTubeVideo;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import reactor.util.annotation.Nullable;

@Data
public class Recommendation {
	private String videoId;
	private String explanation;

	@Nullable
	private YouTubeVideo video;







}
