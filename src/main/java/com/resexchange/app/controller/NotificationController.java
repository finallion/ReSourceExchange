package com.resexchange.app.controller;

import ch.qos.logback.core.model.Model;
import com.resexchange.app.model.Notification;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.NotificationService;
import com.resexchange.app.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping
    public ResponseEntity<?> getAllNotifications(Principal principal,
                                                 RedirectAttributes redirectAttributes) {
        try {
            User loggedInUser = userRepository.findByMail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

            List<Notification> notifications = notificationService.getAllNotifications(loggedInUser.getId());
            int count = notifications.size();

            return ResponseEntity.ok(new NotificationResponse(count, notifications));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            String errorMessage = "Ein Fehler ist beim Abrufen der Benachrichtigungen aufgetreten.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }


    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        Optional<Notification> notification = notificationService.getNotificationById(id);
        if (notification.isPresent()) {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public String getNotification(@PathVariable Long id, Model model) {
        LOGGER.info("Attempting to retrieve notification with ID: {}", id);

        try {
            Notification notification = notificationService.getNotificationById(id)
                    .orElseThrow(() -> new NoSuchElementException("Notification with ID " + id + " not found"));

            LOGGER.info("Successfully retrieved notification with ID: {}", id);

        } catch (NoSuchElementException e) {
            LOGGER.error("Notification with ID {} not found", id, e);
            return "error";
        }

        return "notification-detail";
    }

    // Innere Klasse für JSON-Antwort
    static class NotificationResponse {
        private int count;
        private List<Notification> notifications;

        public NotificationResponse(int count, List<Notification> notifications) {
            this.count = count;
            this.notifications = notifications;
        }

        public int getCount() {
            return count;
        }

        public List<Notification> getNotifications() {
            return notifications;
        }
    }
}