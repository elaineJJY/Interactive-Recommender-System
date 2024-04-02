package de.tum.rs.repository;


import de.tum.rs.dao.Interaction;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@Document(indexName = "interaction")
public interface InteractionRepository  extends ElasticsearchRepository<Interaction, String> {

}
