package is.hi.hbv501g.loot.repository;

import is.hi.hbv501g.loot.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    Optional<Card> findByName(String name);
}
