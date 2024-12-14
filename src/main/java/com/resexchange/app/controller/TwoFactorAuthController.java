package com.resexchange.app.controller;


import com.resexchange.app.security.TwoFactorAuthHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TwoFactorAuthController {

    @GetMapping("/verify-2fa")
    public String show2faForm() {
        return "2fa";
    }

    @PostMapping("/process-2fa")
    public String process2fa(@RequestParam("code") String code, Authentication authentication, HttpSession session, Model model) {
        String username = authentication.getName();
        String expectedCode = TwoFactorAuthHandler.user2faCodes.get(username);

        if (expectedCode != null && expectedCode.equals(code)) {
            session.setAttribute("2fa_passed", true);
            TwoFactorAuthHandler.user2faCodes.remove(username);
            return "redirect:/main";
        } else {
            model.addAttribute("error", "Invalid code. Please try again.");
            return "2fa";
        }
    }
}

