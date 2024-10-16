package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.User;
import is.hi.hbv501g.loot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

//    @GetMapping
//    public String getUsers(){
//        return "Hello API";
//    }

    @Autowired
    private UserRepository userRepository;

//    @GetMapping
//    public List<User> getUser() {
//        return Arrays.asList(new User(1L, "Thorri", "Thorri@gmail.com"),
//                new User(1L, "Bragi", "Bragi@gmail.com"),
//                new User(1L, "Sandesh", "Sandesh@gmail.com"),
//                new User(1L, "oli", "oli@gmail.com"));
//    }

    @GetMapping
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            return "User with ID " + id + " murderd.";
        } else {
            return "User with ID " + id + "escaped.";
        }
    }
}
