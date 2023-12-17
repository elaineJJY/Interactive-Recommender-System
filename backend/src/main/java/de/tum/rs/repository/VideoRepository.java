package de.tum.rs.repository;

import de.tum.rs.dao.YouTubeVideo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@Document(indexName = "videos")
public interface VideoRepository extends ElasticsearchRepository<YouTubeVideo, String> {
	Page<YouTubeVideo> findAll(Pageable pageable);

	@Query("{\"query_string\" : {\"query\" : \"?0\"}}")
	Page<YouTubeVideo> findByKeyword(String keyword, Pageable pageable);
}
