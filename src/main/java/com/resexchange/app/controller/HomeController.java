package com.resexchange.app.controller;

import com.resexchange.app.model.*;
import com.resexchange.app.repositories.BookmarkRepository;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MaterialRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.security.REUserDetails;
import com.resexchange.app.services.BookmarkService;
import com.resexchange.app.services.GeocodingService;
import com.resexchange.app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.List;
import java.util.Locale;

/**
 * Der HomeController ist ein Spring MVC-Controller, der für die Verwaltung der Startseite der Anwendung zuständig ist.
 *
 * Diese Klasse enthält Endpunkte, die für das Rendern der Startseite oder der Hauptansicht verantwortlich sind.
 * Der Controller verarbeitet die grundlegenden Anfragen und sorgt dafür, dass die Benutzer mit der Homepage
 * oder anderen allgemeinen Seiten der Anwendung verbunden werden.
 *
 * @author Dominik, Lion, Stefan
 */
@Controller
public class HomeController {

    @Autowired
    private ListingRepository listingRepository ;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserService userService;

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen auf die Hauptseite der Anwendung.
     *
     * Diese Methode zeigt die Willkommensseite des Benutzers an und liefert alle erforderlichen Daten,
     * um die Benutzeroberfläche korrekt darzustellen. Sie enthält Informationen über den angemeldeten Benutzer,
     * die aktuelle Sprache, die ausgewählte Währung, die Listings auf der Plattform und die gespeicherten Bookmarks des Benutzers.
     * Außerdem wird der Benutzerstandort abgerufen und auf der Seite angezeigt, falls verfügbar.
     *
     * @author Dominik, Lion, Stefan
     * @param model das Spring Model, das die Daten für die View enthält
     * @param request das HttpServletRequest-Objekt, das Informationen zur aktuellen Anfrage enthält
     * @param session die aktuelle HttpSession, um Benutzerspezifische Daten zu verwalten
     * @return der Name der View, die die Hauptseite darstellt ("main")
     */
    @GetMapping("/main")
    public String welcomePage(Model model, HttpServletRequest request, HttpSession session) {
        // Abrufen des Benutzernamens aus dem Security Context
        Locale locale = RequestContextUtils.getLocale(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        REUserDetails userDetails = (REUserDetails) authentication.getPrincipal();
        model.addAttribute("name", userDetails.getUsername());
        model.addAttribute("currentLocale", locale.getLanguage());

        /** Wenn keine Währung festgelegt ist soll Euro angezeigt werden **/
        String currency = (String) session.getAttribute("currency");
        if (currency == null) {
            currency = "eur"; // Standardwährung
            session.setAttribute("currency", currency);
            session.setAttribute("exchange_rate", 1);
        }
        model.addAttribute("currency", currency);

        // Abrufen der nicht verkauften Listings und Hinzufügen zum Model
        List<Listing> listings = listingRepository.findBySoldFalse();
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("listings", listings);
        model.addAttribute("materials", materials);

        Long userId = userDetails.getId();  // Holen der Benutzer-ID
        List<Bookmark> userBookmarks = bookmarkRepository.findByUserId(userId);
        model.addAttribute("userBookmarks", userBookmarks);

        List<Listing> userListings = listingRepository.findByCreatedById(userId);
        model.addAttribute("userListings", userListings);

        // Abrufen der Geokoordinaten des Benutzers
        double[] coordinates = userService.getGeocodedAddressFromUser(userDetails.getUser());

        if (coordinates != null) {
            model.addAttribute("userLatitude", coordinates[0]);
            model.addAttribute("userLongitude", coordinates[1]);
        } else {
            // Fallback Berlin
            model.addAttribute("userLatitude", 52.520008);
            model.addAttribute("userLongitude", 13.404954);
        }

        return "main";  // Weiterleitung zur 'main.html'-Seite
    }
}
