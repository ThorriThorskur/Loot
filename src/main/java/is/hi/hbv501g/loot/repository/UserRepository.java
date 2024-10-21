package is.hi.hbv501g.loot.repository;

import is.hi.hbv501g.loot.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a UserEntity by its ID and fetches the associated Inventory and Cards eagerly.
     *
     * @param id The ID of the user.
     * @return An Optional containing the UserEntity with its Inventory and Cards, or empty if not found.
     */
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.inventory i JOIN FETCH i.cards WHERE u.id = :id")
    Optional<UserEntity> findByIdWithInventoryAndCards(@Param("id") Long id);
}
