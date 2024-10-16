package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserEntity> findAll(){
        // Business logic can be added here if needed
        return userRepository.findAll();
    }

    public UserEntity save(UserEntity userEntity){
        // Business logic, validation, etc.
        return userRepository.save(userEntity);
    }

    public Optional<UserEntity> findById(Long id){
        return userRepository.findById(id);
    }

    public void deleteById(Long id){
        userRepository.deleteById(id);
    }
}
