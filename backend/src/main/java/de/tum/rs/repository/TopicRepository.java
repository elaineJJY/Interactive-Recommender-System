package de.tum.rs.repository;

import de.tum.rs.dao.Topic;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@Document(indexName = "topics")
public interface TopicRepository extends ElasticsearchRepository<Topic, Integer> {

}
