package com.resexchange.app.security;

import com.resexchange.app.model.User;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.MailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOError;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Component
public class TwoFactorAuthHandler implements AuthenticationSuccessHandler {

    @Autowired
    private MailService emailService;

    @Autowired
    private UserRepository userRepository;
    private static final SecureRandom random = new SecureRandom();

    public static final Map<String, String> user2faCodes = new HashMap<>();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String username = authentication.getName();

        User loggedInUser = userRepository.findUserByMail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (loggedInUser.has2FA()) {
            String code = String.format("%06d", random.nextInt(999999));
            user2faCodes.put(username, code);
            emailService.send2faCode(username, code);
            response.sendRedirect("/verify-2fa");
        } else {
            response.sendRedirect("/main");
        }
    }
}