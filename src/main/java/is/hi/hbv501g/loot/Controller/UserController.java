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

    /**
     * Render the frontpage for the site
     *
     * @param userDetails Attributes for the current user in session if any
     * @param model For adding the attributes from userDetails to the html template
     * @return
     */
    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("userDetails", userDetails);
        return "index";
    }

    /**
     * Render the user page
     *
     * @param userDetails Attributes from the current user in session.
     * @param model For adding the attributes from userDetails to the html template
     * @return
     */
    @GetMapping("/user")
    public String userProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        UserEntity user = userService.findByUsername(userDetails.getUsername()).orElse(null);

        model.addAttribute("user", user);
        return "user"; // Render the profile page
    }

    /**
     * Render the admin page for viewing the database and editing all users.
     *
     * @param model For appearing the user details of all users.
     * @return
     */
    @GetMapping("/user/admin")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user_management";
    }

    // TODO: Questionable function maybe find another way to delete users.
    @GetMapping("/deleteuser/{userId}")
    public String deleteUser(@PathVariable("userId") long id, Model model) {
        userService.deleteById(id);
        return "redirect:/user_management";
    }

}