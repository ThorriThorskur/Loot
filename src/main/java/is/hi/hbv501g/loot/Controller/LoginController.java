package is.hi.hbv501g.loot.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @GetMapping("/")
    public String home() {
        return "Hello, Home!";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured!";
    }
}
