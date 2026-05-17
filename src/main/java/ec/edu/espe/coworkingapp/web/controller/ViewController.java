package ec.edu.espe.coworkingapp.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("activePage", "members");
        return "members/list";
    }

    @GetMapping("/workspaces")
    public String workspaces(Model model) {
        model.addAttribute("activePage", "workspaces");
        return "workspaces/list";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("activePage", "bookings");
        return "booking/list";
    }
}