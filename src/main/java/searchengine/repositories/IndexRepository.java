package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexing;

@Repository
public interface IndexRepository extends CrudRepository<Indexing, Integer> {
}
