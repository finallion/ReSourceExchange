package com.resexchange.app.services;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Notification;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.BookmarkRepository;
import com.resexchange.app.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service-Klasse, die Geschäftslogik für die Verwaltung von Bookmarks (Lesezeichen) bereitstellt.
 * Diese Klasse bietet Funktionen zum Hinzufügen, Abrufen, Löschen von Bookmarks und zur Überprüfung,
 * ob ein Benutzer bereits ein bestimmtes Listing als Bookmark gespeichert hat.
 *
 * @author Dominik
 */
@Service
    public class BookmarkService {

        @Autowired
        private BookmarkRepository bookmarkRepository;

        @Autowired
        private NotificationService notificationService;

        private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkService.class);

    /**
     * Fügt ein neues Bookmark für einen Benutzer und ein Listing hinzu.
     *
     * @param user    der Benutzer, der das Bookmark erstellt
     * @param listing das Listing, das als Bookmark gespeichert werden soll
     *
     * @author Dominik
     */
        public void addBookmark(User user, Listing listing) {
            LOGGER.info("Attempting to add a bookmark for user: {} and listing: {}", user.getId(), listing.getId());
            try {
                Bookmark bookmark = new Bookmark(listing, user);
                bookmark.setUser(user);
                bookmark.setListing(listing);
                LOGGER.debug("Created Bookmark object: {}", bookmark);

                bookmarkRepository.save(bookmark);
                LOGGER.info("Successfully saved bookmark for user: {} and listing: {}", user.getId(), listing.getId());

                User creator = listing.getCreatedBy(); // assuming Listing has a `createdBy` field
                if (creator != null && !creator.equals(user)) {
                    String msg = ("Your listing '" + listing.getMaterial().getName() + "' was bookmarked by " + user.getName());
                    String link = "/listing/" + listing.getId();
                    notificationService.createNotification(creator,msg,link);
                    LOGGER.info("Notification created for listing creator: {} about the bookmark.", creator.getId());
                }
            } catch (Exception e) {
                LOGGER.error("Error while adding bookmark for user: {} and listing: {}", user.getId(), listing.getId(), e);
            }
        }

    /**
     * Sucht ein Bookmark anhand seiner ID.
     *
     * @param id die ID des Bookmarks
     * @return das gefundene Bookmark
     * @throws IllegalArgumentException wenn kein Bookmark mit der angegebenen ID gefunden wird
     * @author Dominik
     */
    public Bookmark findById(Long id) {
        LOGGER.info("Looking for bookmark with id: {}", id);

        try {
            Bookmark bookmark = bookmarkRepository.findById(id)
                    .orElseThrow(() -> {
                        LOGGER.warn("Bookmark not found with id(search): {}", id);
                        return new IllegalArgumentException("Bookmark not found with id: " + id);
                    });

            LOGGER.info("Successfully found bookmark with id: {}", id);
            return bookmark;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error occurred while finding bookmark with id: {}", id, e);
            throw e;
        }
    }

    /**
     * Löscht ein Bookmark anhand seiner ID.
     *
     * @param id die ID des zu löschenden Bookmarks
     * @throws IllegalArgumentException wenn kein Bookmark mit der angegebenen ID gefunden wird
     * @author Dominik
     */
    public void deleteBookmark(Long id) {
        LOGGER.info("Attempting to delete bookmark with id: {}", id);

        try {
            if (!bookmarkRepository.existsById(id)) {
                LOGGER.warn("Bookmark not found with id(delete): {}", id);
                throw new IllegalArgumentException("Bookmark not found with id: " + id);
            }

            bookmarkRepository.deleteById(id);
            LOGGER.info("Successfully deleted bookmark with id: {}", id);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error occurred while attempting to delete bookmark with id: {}", id, e);
            throw e;
        }
    }

    /**
     * Überprüft, ob ein bestimmtes Listing bereits als Bookmark für einen Benutzer gespeichert wurde.
     *
     * @param user    der Benutzer, dessen Bookmarks überprüft werden
     * @param listing das Listing, das überprüft werden soll
     * @return true, wenn das Listing bereits als Bookmark für den Benutzer existiert, andernfalls false
     * @author Dominik
     */
    public boolean BookmarkExist(User user, Listing listing) {
        LOGGER.info("Checking if bookmark exists for user: {} and listing: {}", user.getId(), listing.getId());

        boolean exists = bookmarkRepository.existsByUserAndListing(user, listing);

        if (exists) {
            LOGGER.info("Bookmark exists for user: {} and listing: {}", user.getId(), listing.getId());
        } else {
            LOGGER.warn("No bookmark found for user: {} and listing: {}", user.getId(), listing.getId());
        }

        return exists;
    }
}