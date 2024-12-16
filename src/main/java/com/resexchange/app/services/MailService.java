package com.resexchange.app.services;

import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class MailService {

    @Autowired
    private UserRepository userRepository;
    private final JavaMailSender mailSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String content) {
        LOGGER.info("Preparing to send email to: {} with subject: {}", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            LOGGER.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Error occurred while sending email to: {}", to, e);
            throw new RuntimeException("Error occurred while sending email to: " + to, e); // Optional: Exception weiterwerfen
        }
    }

    public void send2faCode(String username, String code) {
        LOGGER.info("Attempting to send 2FA code to user: {}", username);

        try {
            String userEmail = userRepository.findUserByMail(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"))
                    .getMail();

            LOGGER.info("Found email address for user {}: {}", username, userEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Your 2FA Code");
            message.setText("Your verification code is: " + code);

            mailSender.send(message);

            LOGGER.info("Successfully sent 2FA code to: {}", userEmail);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("User not found for 2FA code: {}", username);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error occurred while sending 2FA code to user: {}", username, e);
            throw new RuntimeException("Error occurred while sending 2FA code to user: " + username, e);
        }
    }
}
