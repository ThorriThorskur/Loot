package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Display the login site
     *
     * @return view login.html
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Render Thymeleaf template for registration
    }

    /**
     * Attempt to login and start session
     *
     * @param username Username attribute from the submission form
     * @param password Password attribute from the submission form
     * @param redirectAttributes Error massage if needed
     * @return view index.html if success else redirect back to login.html
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, RedirectAttributes redirectAttributes) {
        Optional<UserEntity> possibleUser = loginService.findByUsername(username);
        if (possibleUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Incorrect username");
            return "redirect:/login";
        }

        UserEntity user = possibleUser.get();

        if (passwordEncoder.matches(user.getPassword(), password)) {
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Incorrect password");
            return "redirect:/login";
        }
    }
}
