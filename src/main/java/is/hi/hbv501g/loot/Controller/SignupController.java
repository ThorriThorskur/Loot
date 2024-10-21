package is.hi.hbv501g.loot.controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    @Autowired
    private final UserService userService;

    public SignupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        // Add an empty SignupRequest object to the model to bind the form data
        model.addAttribute("signupRequest", new UserEntity());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute UserEntity user, Model model) {
        try {
            // Register the new user
            userService.save(user);
            // Redirect to the login page after successful registration
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            // If there was an error (e.g., username taken), show an error message
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
