package de.tum.rs.repository;

import de.tum.rs.dao.YouTubeVideo;
import java.util.List;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Window;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@Document(indexName = "videos")
public interface VideoRepository extends ElasticsearchRepository<YouTubeVideo, String> {

	Page<YouTubeVideo> findAll(Pageable pageable);

	// if the keyword is in the title/description/tags, then return the video
	@Query("{\n" +
		"  \"bool\": {\n" +
		"    \"should\": [\n" +
		"      {\"wildcard\": {\"snippet.title\": \"*?0*\"}},\n" +
		"      {\"wildcard\": {\"snippet.description\": \"*?0*\"}},\n" +
		"      {\"wildcard\": {\"snippet.tags\": \"*?0*\"}}\n" +
		"    ],\n" +
		"    \"minimum_should_match\": 1\n" +
		"  }\n" +
		"}")
	Page<YouTubeVideo> findByKeyword(String keyword, Pageable pageable);


}
