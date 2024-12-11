package com.resexchange.app.controller;

import com.resexchange.app.model.Admin;
import com.resexchange.app.model.Company;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller to handle basic auth
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("privateUser", new PrivateUser());
        model.addAttribute("company", new Company());
        return "register";
    }

    @PostMapping("/register/private")
    public String registerPrivateUser(PrivateUser privateUser, RedirectAttributes redirectAttributes) {
        userService.registerPrivateUser(privateUser);
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
        return "redirect:/login";
    }

    @PostMapping("/register/company")
    public String registerCompanyUser(Company company, RedirectAttributes redirectAttributes) {
        userService.registerCompanyUser(company);
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
        return "redirect:/login";
    }

    @PostMapping("/register/admin")
    public String registerAdmin(Admin admin) {
        userService.registerAdmin(admin);
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token) {
        System.out.println("Verification token received: " + token);
        try {
            userService.verifyUser(token);
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/login?error=invalid_token";
        }
    }
}
