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

@Service
public class NotificationService {

    @Autowired
    private  NotificationRepository notificationRepository;

    public void createNotification(User user, String message,String link) {
        Notification notification = new Notification(message,user,false);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications(Long userId) {
        try {
            return notificationRepository.findByUserIdAndCheckedFalse(userId);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Abrufen der Benachrichtigungen", e);
        }
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
