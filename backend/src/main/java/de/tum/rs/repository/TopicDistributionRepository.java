package de.tum.rs.repository;

import de.tum.rs.dao.Feedback;
import de.tum.rs.dao.TopicDistribution;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


@Document(indexName = "topic_distributions")
public interface TopicDistributionRepository  extends ElasticsearchRepository<TopicDistribution, String> {

}
