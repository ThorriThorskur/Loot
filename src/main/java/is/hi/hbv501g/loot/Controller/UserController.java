package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // New method to handle root URL
    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("userDetails", userDetails);
        return "index";
    }

    @GetMapping("/user")
    public String userProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        UserEntity user = userService.findByUsername(userDetails.getUsername()).orElse(null);

        model.addAttribute("user", user);
        return "user"; // Render the profile page
    }

    @GetMapping("/user/admin")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user_management";
    }

    @GetMapping("/deleteuser/{userId}")
    public String deleteUser(@PathVariable("userId") long id, Model model) {
        userService.deleteById(id);
        return "redirect:/user_management";
    }

}