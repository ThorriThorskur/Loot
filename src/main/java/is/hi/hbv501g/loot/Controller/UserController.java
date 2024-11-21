package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * Display the Dashboard where you can access all the endpoints.
     *
     * @param userDetails User in session details.
     * @param model html attributes
     * @return view index.html
     */
    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            // Handle the case where the user is not authenticated
            model.addAttribute("message", "Welcome, Guest!");
        } else {
            // Add user details to the model
            model.addAttribute("userDetails", userDetails);
            model.addAttribute("username", userDetails.getUsername());
        }
        return "index";
    }


    /**
     * Display the user management site where you can change user details.
     *
     * @param model html attributes
     * @return view user_management.html
     */
    @GetMapping("/user_management")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user_management";
    }

    /**
     * Remove a user from the database.
     *
     * @param id Id of the selected user in the html
     * @param redirectAttributes Attribute to display error or success information
     * @return view user_management.html with success or error message
     */
    @PostMapping("/deleteuser")
    public String deleteUser(@RequestParam("id") long id,
                             RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists before attempting to delete
            if (userService.findById(id).isPresent()) {
                userService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "User successfully deleted.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            }
        } catch (Exception e) {
            // Log the error (ideally use a logging framework)
            System.err.println("Error deleting user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the user.");
        }
        return "redirect:/user_management";
    }

}