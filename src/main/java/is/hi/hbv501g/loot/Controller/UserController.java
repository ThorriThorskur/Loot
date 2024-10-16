// File: src/main/java/is/hi/hbv501g/loot/Controller/WebController.java

package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController{

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        List<UserEntity> users = userService.findAll();
        model.addAttribute("users", users);
        return "index";
    }

    @GetMapping("/adduser")
    public String addUserForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "adduser";
    }

    @PostMapping("/adduser")
    public String addUserSubmit(@ModelAttribute UserEntity user) {
        userService.save(user);
        return "redirect:/";
    }

    @GetMapping("/deleteuser/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/";
    }
}
