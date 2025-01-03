package com.resexchange.app.controller;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.services.BookmarkService;
import com.resexchange.app.services.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Der BookmarkController ist ein Spring MVC-Controller, der für die Verwaltung von Bookmarks zuständig ist.
 *
 * Diese Klasse enthält alle Endpunkte für das Erstellen, Löschen und Anzeigen von Bookmarks in der Anwendung.
 * Bookmarks repräsentieren Benutzer-Lesezeichen, die mit Listings auf der Plattform verknüpft sind.
 * Der Controller stellt Funktionen zur Verfügung, um Bookmarks hinzuzufügen, zu entfernen und anzuzeigen.
 *
 * @author Dominik
 */
@Controller
@RequestMapping("/bookmark")
public class BookmarkController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkController.class);

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private ListingService listingService;

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, die die Details eines Bookmarks anzeigt.
     *
     * Diese Methode wird aufgerufen, um die Details eines bestimmten Bookmarks anhand seiner ID abzurufen.
     * Das Bookmark wird gefunden, und das zugehörige Listing wird geladen. Beide Informationen werden dann
     * an die View übergeben, um dem Benutzer die entsprechenden Details anzuzeigen.
     *
     * @author Dominik
     * @param id die ID des Bookmarks, dessen Details angezeigt werden sollen
     * @param model das Spring Model, das die Attribute für die View enthält
     * @return der Name der Ansicht, die die Details des Bookmarks und des zugehörigen Listings anzeigt
     */
    @GetMapping("/{id}")
    public String getBookmarkDetails(@PathVariable Long id, Model model) {
        LOGGER.info("Fetching bookmark details for bookmark ID: {}", id);

        try {
            Bookmark bookmark = bookmarkService.findById(id);
            Listing listing = listingService.getListingById(bookmark.getListing().getId());

            model.addAttribute("bookmark", bookmark);
            model.addAttribute("listing", listing);

            LOGGER.info("Successfully fetched bookmark and associated listing for bookmark ID: {}", id);

            return "listing-detail";
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching bookmark details for bookmark ID: {}", id, e);
            return "error";
        }
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um ein Bookmark zu löschen.
     *
     * Diese Methode wird aufgerufen, wenn ein Benutzer ein Bookmark entfernen möchte. Sie löscht das Bookmark
     * anhand der angegebenen ID und leitet den Benutzer anschließend zurück zur Hauptseite.
     *
     * @author Dominik
     * @param id die ID des Bookmarks, das gelöscht werden soll
     * @return eine Weiterleitung zur Hauptseite ("/main")
     */
    @GetMapping("/delete/{id}")
    public String deleteBookmark(@PathVariable Long id) {
        LOGGER.info("Attempting to delete bookmark with ID: {}", id);

        try {
            bookmarkService.deleteBookmark(id);
            LOGGER.info("Bookmark with ID: {} successfully deleted", id);

            return "redirect:/main";
        } catch (Exception e) {
            LOGGER.error("Error occurred while deleting bookmark with ID: {}", id, e);
            return "error";
        }
    }


}

