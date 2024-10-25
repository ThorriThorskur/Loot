package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity()); // Pass a new UserEntity to the form
        return "register"; // Render Thymeleaf template for registration
    }

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
}
