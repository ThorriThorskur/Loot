package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Render the html template for register.html
     *
     * @param model
     * @return
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity()); // Pass a new UserEntity to the form
        return "register"; // Render Thymeleaf template for registration
    }

    /**
     * Register a new user to the database through html template.
     *
     * @param user Attributes in the html form
     * @param model For error handling
     * @return
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserEntity user, Model model) {
        if (userService.userExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user); // Save the user
        return "redirect:/login?success"; // Redirect to login page after successful signup
    }

    /**
     * Register a new user for rest api.
     *
     * @param user JSON form for username and password.
     * @return
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerUserApi(@RequestBody UserEntity user) {
        Logger logger = LoggerFactory.getLogger(RegisterController.class);

        logger.info("Received request to register user: " + user.getUsername());

        if (userService.userExists(user.getUsername())) {
            logger.warn("Username already exists: " + user.getUsername());
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);

        logger.info("User registered successfully: " + user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }
}
