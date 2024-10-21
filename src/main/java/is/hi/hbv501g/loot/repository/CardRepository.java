package is.hi.hbv501g.loot.repository;

import is.hi.hbv501g.loot.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByName(String name);
}

