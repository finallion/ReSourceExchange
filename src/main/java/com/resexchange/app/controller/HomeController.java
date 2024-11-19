package com.resexchange.app.controller;

import com.resexchange.app.model.Listing;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.security.REUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    // Repository für Listings wird hier durch Dependency Injection eingefügt
    @Autowired
    private ListingRepository listingRepository ;

    // GET-Methoden für das Abrufen der Listings und Anzeigen im Template
    @GetMapping("/main")
    public String welcomePage(Model model) {
        // Abrufen des Benutzernamens aus dem Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        REUserDetails userDetails = (REUserDetails) authentication.getPrincipal();
        model.addAttribute("name", userDetails.getUsername());

        // Abrufen der Listings aus der Datenbank
        List<Listing> listings = listingRepository.findAll();  // 'findAll()' ist eine Instanzmethode
        model.addAttribute("listings", listings);  // Listings ins Model setzen

        return "main";  // Weiterleitung zur 'main.html'-Seite
    }
}
