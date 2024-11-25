package com.resexchange.app.controller;

import com.resexchange.app.model.Listing;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.security.REUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.List;
import java.util.Locale;

@Controller
public class HomeController {

    // Repository für Listings wird hier durch Dependency Injection eingefügt
    @Autowired
    private ListingRepository listingRepository ;

    // GET-Methoden für das Abrufen der Listings und Anzeigen im Template
    @GetMapping("/main")
    public String welcomePage(Model model, HttpServletRequest request) {
        // Abrufen des Benutzernamens aus dem Security Context
        Locale locale = RequestContextUtils.getLocale(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        REUserDetails userDetails = (REUserDetails) authentication.getPrincipal();
        model.addAttribute("name", userDetails.getUsername());
        model.addAttribute("currentLocale", locale.getLanguage());
        // Abrufen der Listings aus der Datenbank
        List<Listing> listings = listingRepository.findAll();  // 'findAll()' ist eine Instanzmethode
        model.addAttribute("listings", listings);  // Listings ins Model setzen

        return "main";  // Weiterleitung zur 'main.html'-Seite
    }
}
