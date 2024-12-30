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

/**
 * Controller zur Verwaltung von Benachrichtigungen im System.
 * Bietet Endpunkte zum Abrufen, Löschen und Anzeigen von Benachrichtigungen für einen Benutzer.
 *
 * @author Dominik
 */
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


    /**
     * Ruft alle Benachrichtigungen des angemeldeten Benutzers ab.
     *
     * Diese Methode holt die Benachrichtigungen aus der Datenbank, zählt sie und gibt sie an den Client zurück.
     * Im Falle von Fehlern (z. B. Benutzer nicht gefunden) wird eine Fehlermeldung zurückgegeben.
     *
     * @param principal Das Principal-Objekt des angemeldeten Benutzers, das Benutzerinformationen enthält.
     * @param redirectAttributes Attribute, die beim Weiterleiten im Falle von Fehlern verwendet werden.
     * @return Ein ResponseEntity, das die Liste der Benachrichtigungen und deren Anzahl oder eine Fehlermeldung enthält.
     * @author Dominik
     */
    @GetMapping
    public ResponseEntity<?> getAllNotifications(Principal principal,
                                                 RedirectAttributes redirectAttributes) {
        LOGGER.info("Fetching all notifications for the logged-in user: {}", principal.getName());
        try {
            User loggedInUser = userRepository.findByMail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

            List<Notification> notifications = notificationService.getAllNotifications(loggedInUser.getId());
            int count = notifications.size();

            LOGGER.info("Successfully fetched {} notifications for user: {}", count, principal.getName());
            return ResponseEntity.ok(new NotificationResponse(count, notifications));

        } catch (IllegalArgumentException e) {
            LOGGER.error("Error fetching notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            LOGGER.error("An error occurred while fetching notifications", e);
            String errorMessage = "Ein Fehler ist beim Abrufen der Benachrichtigungen aufgetreten.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }


    /**
     * Löscht eine Benachrichtigung anhand ihrer ID.
     *
     * Diese Methode löscht die Benachrichtigung aus der Datenbank basierend auf ihrer ID.
     * Wenn die Benachrichtigung nicht gefunden wird, wird eine "Nicht gefunden"-Antwort zurückgegeben.
     *
     * @param id Die ID der zu löschenden Benachrichtigung.
     * @return Ein ResponseEntity, das den Erfolg oder Fehler der Löschung anzeigt.
     * @author Dominik
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        LOGGER.info("Attempting to delete notification with ID: {}", id);

        Optional<Notification> notification = notificationService.getNotificationById(id);
        if (notification.isPresent()) {
            notificationService.deleteNotification(id);
            LOGGER.info("Successfully deleted notification with ID: {}", id);
            return ResponseEntity.ok().build();
        } else {
            LOGGER.error("Notification with ID {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ruft eine Benachrichtigung anhand ihrer ID ab.
     *
     * Diese Methode holt eine einzelne Benachrichtigung aus der Datenbank basierend auf ihrer ID.
     * Wenn die Benachrichtigung nicht gefunden wird, wird eine Fehlerseite zurückgegeben.
     *
     * @param id Die ID der abzurufenden Benachrichtigung.
     * @param model Das Model, um Attribute für die Ansicht hinzuzufügen.
     * @return Der Name der zu rendernden Ansicht.
     * @author Dominik
     */
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

    /**
     * Innere Klasse zur Strukturierung der JSON-Antwort mit der Liste der Benachrichtigungen und deren Anzahl.
     * Diese Klasse wird verwendet, um die Daten in einem strukturierten Format im Antwortkörper zu senden.
     *
     * @author Dominik
     */
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