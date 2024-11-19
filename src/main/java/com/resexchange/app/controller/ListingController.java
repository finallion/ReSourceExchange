package com.resexchange.app.controller;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.services.ListingService;
import com.resexchange.app.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/listing")
public class ListingController {

    @Autowired
    private final ListingService listingService;

    @Autowired
    private final MaterialRepository materialRepository;

    @Autowired
    private final ListingRepository listingRepository;

    public ListingController(ListingService listingService, MaterialRepository materialRepository, ListingRepository listingRepository) {
        this.listingService = listingService;
        this.materialRepository = materialRepository;
        this.listingRepository = listingRepository;
    }

    // GET-Request, um das Formular für ein neues Listing zu zeigen
    @GetMapping("/create")
    public String showListingForm(Model model) {
        // Ein leeres Listing-Objekt hinzufügen
        model.addAttribute("listing", new Listing());

        // Liste aller vorhandenen Materialien für die Auswahl bereitstellen
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);

        return "CreateListings";  // Thymeleaf-Template für das Formular
    }

    // POST-Request, um ein neues Listing zu speichern
    @PostMapping("/create")
    public String createListing(@ModelAttribute("listing") Listing listing, RedirectAttributes redirectAttributes) {
        // Überprüfe, ob Material vorhanden ist
        if (listing.getMaterial() == null || listing.getMaterial().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Material must be selected!");
            return "redirect:/listing/create";  // Zurück zum Formular, wenn Material nicht ausgewählt wurde
        }

        // Validierung der anderen Felder (z.B. Menge und Preis)
        if (listing.getQuantity() <= 0 || listing.getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity and Price must be greater than 0!");
            return "redirect:/listing/create";  // Zurück zum Formular bei fehlerhaften Eingabewerten
        }

        // Das Listing speichern
        listingRepository.save(listing);
        redirectAttributes.addFlashAttribute("success", "Listing successfully created!");

        // Weiterleitung zur Hauptseite oder zum Listing-Bereich
        return "redirect:/main";  // Oder eine andere Route, z.B. "/listings"
    }

    // GET-Request, um alle Listings anzuzeigen
    @GetMapping("/main")
    public String showListings(Model model) {
        // Alle Listings aus der Datenbank abrufen
        List<Listing> listings = listingRepository.findAll();
        model.addAttribute("listings", listings);  // Listings an das Model übergeben

        return "main";  // Das Template, das die Listings anzeigt
    }

    /*
    // GET-Request, um ein bestimmtes Listing anzuzeigen
    @GetMapping("/view/{id}")
    public String viewListing(@PathVariable("id") Long id, Model model) {
        Optional<Listing> listing = listingService.getListingById(id);
        if (listing.isPresent()) {
            model.addAttribute("listing", listing.get());
            return "viewListing";  // Thymeleaf-Template für die Detailansicht eines Listings
        } else {
            return "redirect:/listing/list";  // Zurück zur Liste, falls das Listing nicht gefunden wurde
        }
    }

    // GET-Request, um ein Listing zu löschen
    @GetMapping("/delete/{id}")
    public String deleteListing(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        listingService.deleteListing(id);
        redirectAttributes.addFlashAttribute("successMessage", "Listing successfully deleted!");
        return "redirect:/listing/list";
    }*/
}
