package com.resexchange.app.controller;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.ListingService;
import com.resexchange.app.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/listing")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    // GET-Request, um das Formular für ein neues Listing zu zeigen
    @GetMapping("/create")
    public String showListingForm(Model model) {
        model.addAttribute("listing", new Listing());

        // Liste aller vorhandenen Materialien
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);

        return "CreateListings";
    }

    // POST-Request, um ein neues Listing zu speichern
    @PostMapping("/create")
    public String createListing(@ModelAttribute("listing") Listing listing, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (listing.getMaterial() == null || listing.getMaterial().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Material must be selected!");
            return "redirect:/listing/create";
        }

        if (listing.getQuantity() <= 0 || listing.getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity and Price must be greater than 0!");
            return "redirect:/listing/create";
        }

        User user = userRepository.findByMail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        listing.setCreatedBy(user);
        listingRepository.save(listing);
        redirectAttributes.addFlashAttribute("success", "Listing successfully created!");

        return "redirect:/main";
    }

    // GET-Request, um alle Listings anzuzeigen
    @GetMapping("/main")
    public String showListings(Model model) {
        List<Listing> listings = listingRepository.findAll();
        model.addAttribute("listings", listings);
        return "main";
    }

    @GetMapping("/update/{id}/{initiatorId}")
    public String showUpdateForm(@PathVariable("id") Long id,
                                 @PathVariable("initiatorId") Long initiatorId,
                                 Model model, Principal principal) {

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
        if (!loggedInUser.getId().equals(initiatorId)) {
            throw new SecurityException("Unauthorized access attempt");
        }

        Listing listing = listingService.getListingById(id);
        List<Material> materials = materialRepository.findAll();

        if (!(listing.getMaterial() == null)) {
            listing.setMaterial(listing.getMaterial());
        }else if(!materials.isEmpty()){
            listing.setMaterial(materials.getFirst()); // Setze das erste Material als Standard
        }

        model.addAttribute("listing", listing);
        model.addAttribute("materials", materials);

        return "updateListing";
    }

    @PostMapping("/update/{id}/{initiatorId}")
    public String updateListing(@PathVariable("id") Long id,
                                @PathVariable("initiatorId") Long initiatorId,
                                @ModelAttribute Listing listing,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
        if (!loggedInUser.getId().equals(initiatorId)) {
            throw new SecurityException("Unauthorized update attempt");
        }

        listing.setId(id);
        listing.setCreatedBy(loggedInUser);
        listing.setChats(listing.getChats());

        // Das Listing aktualisieren
        listingService.updateListing(listing);

        redirectAttributes.addFlashAttribute("message", "Listing updated successfully.");
        return "redirect:/main";
    }


    @GetMapping("/delete/{id}")
    public String deleteListing(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        listingService.deleteListing(id);
        redirectAttributes.addFlashAttribute("message", "Listing deleted successfully.");
        return "redirect:/main";
    }

}
