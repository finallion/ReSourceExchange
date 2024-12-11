package com.resexchange.app.services;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.BookmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Fügt ein neues Bookmark für einen Benutzer und ein Listing hinzu.
     *
     * @param user    der Benutzer, der das Bookmark erstellt
     * @param listing das Listing, das als Bookmark gespeichert werden soll
     *
     * @author Dominik
     */
        public void addBookmark(User user, Listing listing) {
            Bookmark bookmark = new Bookmark(listing, user);
            bookmark.setUser(user);
            bookmark.setListing(listing);
            bookmarkRepository.save(bookmark);

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
            return bookmarkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found with id: " + id));
    }

    /**
     * Löscht ein Bookmark anhand seiner ID.
     *
     * @param id die ID des zu löschenden Bookmarks
     * @throws IllegalArgumentException wenn kein Bookmark mit der angegebenen ID gefunden wird
     * @author Dominik
     */
    public void deleteBookmark(Long id) {
            if (!bookmarkRepository.existsById(id)) {
                throw new IllegalArgumentException("Bookmark not found with id: " + id);
            }
            bookmarkRepository.deleteById(id);
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
            return bookmarkRepository.existsByUserAndListing(user, listing);
        }
}