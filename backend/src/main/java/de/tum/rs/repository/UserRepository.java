package de.tum.rs.repository;


import de.tum.rs.dao.User;
import java.util.Optional;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@Document(indexName = "users")
public interface UserRepository extends ElasticsearchRepository<User, String> {

	Optional<User> findByUserId(String userId);
}
