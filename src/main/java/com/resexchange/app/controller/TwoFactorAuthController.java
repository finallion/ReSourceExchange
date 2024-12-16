package com.resexchange.app.controller;


import com.resexchange.app.security.TwoFactorAuthHandler;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TwoFactorAuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwoFactorAuthController.class);

    @GetMapping("/verify-2fa")
    public String show2faForm() {
        LOGGER.info("User is requesting the 2FA verification form");
        return "2fa";
    }

    @PostMapping("/process-2fa")
    public String process2fa(@RequestParam("code") String code, Authentication authentication, HttpSession session, Model model) {
        String username = authentication.getName();
        String expectedCode = TwoFactorAuthHandler.user2faCodes.get(username);

        LOGGER.info("Processing 2FA for user: {}", username);

        if (expectedCode != null && expectedCode.equals(code)) {
            LOGGER.info("2FA verification successful for user: {}", username);
            session.setAttribute("2fa_passed", true);
            TwoFactorAuthHandler.user2faCodes.remove(username);
            return "redirect:/main";
        } else {
            LOGGER.warn("Invalid 2FA code entered for user: {}", username);
            model.addAttribute("error", "Invalid code. Please try again.");
            return "2fa";
        }
    }
}

