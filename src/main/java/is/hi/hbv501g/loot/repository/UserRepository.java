package is.hi.hbv501g.loot.repository;

import is.hi.hbv501g.loot.Entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
}
