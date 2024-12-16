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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.security.Principal;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

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
     * @param page die Seite welche mittels Pagination angezeigt soll
     * @param model das Spring Model, das die Daten für die View enthält
     * @param request das HttpServletRequest-Objekt, das Informationen zur aktuellen Anfrage enthält
     * @param session die aktuelle HttpSession, um Benutzerspezifische Daten zu verwalten
     * @return der Name der View, die die Hauptseite darstellt ("main")
     */
    @GetMapping("/main")
    public String welcomePage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model,
            HttpServletRequest request,
            Principal principal,
            HttpSession session
    ) {
        String user = principal.getName();
        User loggedInUser = userRepository.findByMail(user)
                .orElseThrow(() -> {
                    LOGGER.error("User not found: {}", user);
                    return new ResourceNotFoundException("User not found");
                });
        LOGGER.info("Logged in user: {}", user);

        if (loggedInUser.has2FA()) {
            Boolean tfaPassed = (Boolean) session.getAttribute("2fa_passed");
            if (tfaPassed == null || !tfaPassed) {
                LOGGER.info("2FA not passed for user: {}", user);
                return "redirect:/verify-2fa";
            }
        }

        Locale locale = RequestContextUtils.getLocale(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        REUserDetails userDetails = (REUserDetails) authentication.getPrincipal();
        model.addAttribute("name", userDetails.getUsername());
        model.addAttribute("currentLocale", locale.getLanguage());

        String currency = (String) session.getAttribute("currency");
        if (currency == null) {
            currency = "eur";
            session.setAttribute("currency", currency);
            session.setAttribute("exchange_rate", 1);
            LOGGER.info("No currency set. Defaulting to: {}", currency);
        }
        model.addAttribute("currency", currency);

        Pageable pageable = PageRequest.of(page - 1, 8);
        Page<Listing> listingPage = listingRepository.findBySoldFalse(pageable);
        List<Material> materials = materialRepository.findAll();

        model.addAttribute("listings", listingPage.getContent());
        model.addAttribute("materials", materials);
        model.addAttribute("currentPage", listingPage.getNumber() + 1);
        model.addAttribute("totalPages", listingPage.getTotalPages());

        Long userId = userDetails.getId();
        List<Bookmark> userBookmarks = bookmarkRepository.findByUserId(userId);
        model.addAttribute("userBookmarks", userBookmarks);

        List<Listing> userListings = listingRepository.findByCreatedById(userId);
        model.addAttribute("userListings", userListings);

        double[] coordinates = userService.getGeocodedAddressFromUser(userDetails.getUser());

        if (coordinates != null) {
            model.addAttribute("userLatitude", coordinates[0]);
            model.addAttribute("userLongitude", coordinates[1]);
            LOGGER.info("Geocoded address for user {}: latitude = {}, longitude = {}", user, coordinates[0], coordinates[1]);
        } else {
            model.addAttribute("userLatitude", 52.520008); // Berlin fallback
            model.addAttribute("userLongitude", 13.404954);
            LOGGER.warn("Could not retrieve geocoded address for user: {}. Using fallback coordinates.", user);
        }

        return "main";
    }
}
