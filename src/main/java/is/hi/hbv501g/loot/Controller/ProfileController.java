package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * Displays the user's profile page.
     *
     * @param userDetails The authenticated user's details.
     * @param model       The model to pass data to the view.
     * @return The view template for displaying the profile.
     */
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            model.addAttribute("user", user);

            // Convert profile picture to Base64 string if available
            if (user.getProfilePicture() != null) {
                String base64Image = Base64.getEncoder().encodeToString(user.getProfilePicture());
                model.addAttribute("profilePictureBase64", base64Image);
            } else {
                model.addAttribute("profilePictureBase64", null);
            }
        } else {
            model.addAttribute("error", "User not found.");
            return "error";
        }
        return "profile"; // Render the profile.html page
    }

    /**
     * Handles uploading the user's profile picture.
     *
     * @param userDetails    The authenticated user's details.
     * @param profilePicture The uploaded profile picture file.
     * @param model          The model to pass data to the view.
     * @return Redirects to the profile page.
     */
    @PostMapping("/profile/upload")
    public String uploadProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam("profilePicture") MultipartFile profilePicture,
                                       Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            try {
                user.setProfilePicture(profilePicture.getBytes());
                userService.save(user);
                model.addAttribute("success", "Profile picture uploaded successfully!");
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload profile picture.");
            }
        } else {
            model.addAttribute("error", "User not found.");
        }

        return "redirect:/profile";
    }


    /**
     * Handles removing the user's profile picture.
     *
     * @param userDetails The authenticated user's details.
     * @param model       The model to pass data to the view.
     * @return Redirects to the profile page.
     */
    @PostMapping("/profile/remove")
    public String removeProfilePicture(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            // Set profile picture to null to remove it
            user.setProfilePicture(null);
            userService.save(user);
            model.addAttribute("success", "Profile picture removed successfully!");
        } else {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        return "redirect:/profile";
    }
}
