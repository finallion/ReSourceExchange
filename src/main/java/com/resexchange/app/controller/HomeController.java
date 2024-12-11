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

    // GET-Methoden für das Abrufen der Listings und Anzeigen im Template
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

        List<Listing> listings = listingRepository.findBySoldFalse();
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("listings", listings);
        model.addAttribute("materials", materials);

        Long userId = userDetails.getId();  // Holen der Benutzer-ID
        List<Bookmark> userBookmarks = bookmarkRepository.findByUserId(userId);
        model.addAttribute("userBookmarks", userBookmarks);

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
