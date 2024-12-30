package com.resexchange.app.services;

import com.resexchange.app.model.Notification;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zur Verwaltung von Benachrichtigungen im System.
 * Bietet Methoden zum Erstellen, Abrufen und Löschen von Benachrichtigungen.
 *
 * @author Dominik
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private  NotificationRepository notificationRepository;

    /**
     * Erstellt eine neue Benachrichtigung für den angegebenen Benutzer.
     *
     * Diese Methode erstellt eine Benachrichtigung mit einer Nachricht und einem Link
     * und speichert diese in der Datenbank.
     *
     * @param user Der Benutzer, dem die Benachrichtigung gehört.
     * @param message Die Nachricht der Benachrichtigung.
     * @param link Ein optionaler Link, der mit der Benachrichtigung verknüpft wird.
     * @author Dominik
     */
    public void createNotification(User user, String message,String link) {
        LOGGER.info("Creating notification for user: {}", user.getId());
        Notification notification = new Notification(message,user,false);
        notification.setLink(link);
        notificationRepository.save(notification);

        LOGGER.info("Notification for user {} successfully created", user.getId());
    }

    /**
     * Ruft alle nicht überprüften Benachrichtigungen für den angegebenen Benutzer ab.
     *
     * Diese Methode gibt eine Liste von Benachrichtigungen zurück, die noch nicht vom Benutzer überprüft wurden.
     *
     * @param userId Die ID des Benutzers, dessen Benachrichtigungen abgerufen werden sollen.
     * @return Eine Liste von Benachrichtigungen für den angegebenen Benutzer.
     * @author Dominik
     */
    public List<Notification> getAllNotifications(Long userId) {
        LOGGER.info("Fetching all unchecked notifications for user with ID: {}", userId);
        try {
            List<Notification> notifications = notificationRepository.findByUserIdAndCheckedFalse(userId);
            LOGGER.info("Successfully fetched {} notifications for user with ID {}", notifications.size(), userId);
            return notifications;
        } catch (Exception e) {
            LOGGER.error("Error fetching notifications for user with ID {}", userId, e);
            throw new RuntimeException("Error fetching notifications", e);
        }
    }

    /**
     * Ruft eine Benachrichtigung anhand ihrer ID ab.
     *
     * Diese Methode gibt eine einzelne Benachrichtigung zurück, die anhand ihrer ID gefunden wurde.
     *
     * @param id Die ID der Benachrichtigung, die abgerufen werden soll.
     * @return Ein Optional mit der Benachrichtigung, wenn sie gefunden wurde.
     * @author Dominik
     */
    public Optional<Notification> getNotificationById(Long id) {
        LOGGER.info("Fetching notification with ID: {}", id);
        return notificationRepository.findById(id);
    }

    /**
     * Löscht eine Benachrichtigung anhand ihrer ID.
     *
     * Diese Methode löscht die Benachrichtigung aus der Datenbank basierend auf ihrer ID.
     *
     * @param id Die ID der zu löschenden Benachrichtigung.
     * @author Dominik
     */
    public void deleteNotification(Long id) {
        LOGGER.info("Deleting notification with ID: {}", id);

        try {
            notificationRepository.deleteById(id);
            LOGGER.info("Notification with ID {} successfully deleted", id);
        } catch (Exception e) {
            LOGGER.error("Error deleting notification with ID {}", id, e);
            throw new RuntimeException("Error deleting notification", e);
        }
    }
}
