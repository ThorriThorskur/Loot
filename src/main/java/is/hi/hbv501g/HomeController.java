package is.hi.hbv501g.loot;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index";  // This will render src/main/resources/templates/index.html (if using Thymeleaf)
    }
}
