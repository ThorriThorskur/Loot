package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Method to get all users from the database
     *
     * @return all user details
     */
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    /**
     * Method to get user details with a user id
     *
     * @param id
     * @return user details
     */
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Method to get user details by username.
     *
     * @param username
     * @return user details
     */
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Method to check if a user with that username exists.
     *
     * @param username
     * @return true if user exists else false
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Insert user details to the database
     *
     * @param user user details
     * @return
     */
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Remove user by user id
     *
     * @param id id of user to be removed
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
