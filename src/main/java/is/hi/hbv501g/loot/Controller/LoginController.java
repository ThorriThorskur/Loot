package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new UserEntity()); // Pass a new UserEntity to the form
        return "login"; // Render Thymeleaf template for registration
    }
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, Model model, RedirectAttributes redirectAttributes) {
        Optional<UserEntity> possibleUser = loginService.findByUsername(username);
        if (!possibleUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Incorrect username");
            return "redirect:/login";
        }
        UserEntity user = possibleUser.get();

        if (new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            // TODO: Start session
            return "redirect:/index";
        } else {
            redirectAttributes.addFlashAttribute("error", "Incorrect password");
            return "redirect:/login";
        }
    }
}
