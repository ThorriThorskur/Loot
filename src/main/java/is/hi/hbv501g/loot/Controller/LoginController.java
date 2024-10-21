package is.hi.hbv501g.loot.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class LoginController {

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        boolean loggedIn = (principal != null);
        model.addAttribute("loggedIn", loggedIn);
        return "index";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured!";
    }
}
