package de.tum.rs.repository;

import de.tum.rs.dao.Feedback;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
@Document(indexName = "feedback")
public interface FeedbackRepository extends ElasticsearchRepository<Feedback, String> {

}
