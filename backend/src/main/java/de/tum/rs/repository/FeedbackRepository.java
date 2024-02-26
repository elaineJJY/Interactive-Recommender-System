package de.tum.rs.repository;

import de.tum.rs.dao.Feedback;
import java.util.Date;
import java.util.List;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
@Document(indexName = "feedback")
public interface FeedbackRepository extends ElasticsearchRepository<Feedback, String> {

	// get the feedback by user id with the timetamp bigger than the given timestamp
	List<Feedback> findByUserIdAndTimestampGreaterThan(String userId, Date timestamp);
}
