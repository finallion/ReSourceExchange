package com.resexchange.app.controller;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.services.BookmarkService;
import com.resexchange.app.services.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // Das Bookmark anhand der ID finden
        Bookmark bookmark = bookmarkService.findById(id);

        // Das Listing, das mit dem Bookmark verknüpft ist, finden
        Listing listing = listingService.getListingById(bookmark.getListing().getId());

        // Hinzufügen der Details zum Model, um sie in der View anzuzeigen
        model.addAttribute("bookmark", bookmark);
        model.addAttribute("listing", listing);


        return "listing-detail";
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
    @GetMapping ("/delete/{id}")
    public String deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);

        return "redirect:/main";
    }

}

