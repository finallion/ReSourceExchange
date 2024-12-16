package com.resexchange.app.controller;

import com.resexchange.app.model.Admin;
import com.resexchange.app.model.Company;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        LOGGER.info("Accessing the login page");

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        LOGGER.info("Accessing the registration page");

        model.addAttribute("privateUser", new PrivateUser());
        model.addAttribute("company", new Company());

        LOGGER.info("Registration page populated with new PrivateUser and Company objects");

        return "register";
    }

    @PostMapping("/register/private")
    public String registerPrivateUser(PrivateUser privateUser, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to register private user with email: {}", privateUser.getMail());

        try {
            userService.registerPrivateUser(privateUser);
            LOGGER.info("Private user with email: {} successfully registered", privateUser.getMail());

            redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            LOGGER.error("Error occurred during registration of private user with email: {}", privateUser.getMail(), e);
            redirectAttributes.addFlashAttribute("message", "An error occurred during registration. Please try again.");
            return "redirect:/register";
        }
    }


    @PostMapping("/register/company")
    public String registerCompanyUser(Company company, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to register company with email: {}", company.getMail());

        try {
            userService.registerCompanyUser(company);
            LOGGER.info("Company with email: {} successfully registered", company.getMail());

            redirectAttributes.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            LOGGER.error("Error occurred during registration of company with email: {}", company.getMail(), e);
            redirectAttributes.addFlashAttribute("message", "An error occurred during registration. Please try again.");
            return "redirect:/register";
        }
    }


    @PostMapping("/register/admin")
    public String registerAdmin(Admin admin) {
        LOGGER.info("Attempting to register admin with email: {}", admin.getMail());

        try {
            userService.registerAdmin(admin);
            LOGGER.info("Admin with email: {} successfully registered", admin.getMail());

            return "redirect:/login";
        } catch (Exception e) {
            LOGGER.error("Error occurred during registration of admin with email: {}", admin.getMail(), e);
            return "redirect:/register";
        }
    }


    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token) {
        LOGGER.info("Verification token received: {}", token);

        try {
            userService.verifyUser(token);
            LOGGER.info("User verification successful for token: {}", token);
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid verification token: {}", token, e);
            return "redirect:/login?error=invalid_token";
        }
    }
}
