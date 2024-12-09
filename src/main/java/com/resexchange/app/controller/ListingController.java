package com.resexchange.app.controller;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.resexchange.app.model.Address;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.*;
import com.resexchange.app.repositories.MaterialRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/listing")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private APIContext apiContext;

    @Autowired
    private PaypalService paypalService;

    // GET-Request, um das Formular für ein neues Listing zu zeigen
    @GetMapping("/create")
    public String showListingForm(Model model) {
        model.addAttribute("listing", new Listing());

        // Liste aller vorhandenen Materialien
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);

        return "CreateListings";
    }

    @GetMapping("/purchases")
    public String getUserPurchases(Principal principal, Model model) {
        String mail = principal.getName();
        List<Listing> purchasedListings = listingRepository.findByBuyerMail(mail);
        model.addAttribute("purchasedListings", purchasedListings);
        return "purchases";
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

        Address address = userService.getAddressFromUser(user);

        String street = address.getStreet();
        String city = address.getCity();
        String postalCode = address.getPostalCode();
        String country = address.getCountry();

        double[] coordinates = GeocodingService.getCoordinatesFromAddress(street, city, postalCode, country);

        if (coordinates != null) {
            listing.setLatitude(coordinates[0]);
            listing.setLongitude(coordinates[1]);
        }

        listing.setCreatedBy(user);
        listingRepository.save(listing);
        redirectAttributes.addFlashAttribute("success", "Listing successfully created!");

        return "redirect:/main";
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

        Address address = userService.getAddressFromUser(loggedInUser);

        String street = address.getStreet();
        String city = address.getCity();
        String postalCode = address.getPostalCode();
        String country = address.getCountry();

        double[] coordinates = GeocodingService.getCoordinatesFromAddress(street, city, postalCode, country);

        if (coordinates != null) {
            listing.setLatitude(coordinates[0]);
            listing.setLongitude(coordinates[1]);
        }

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

    @GetMapping("/bookmark/{id}/{initiatorId}")
    public String addBookmark(@PathVariable Long id, @PathVariable Long initiatorId, Principal principal, Authentication authentication,RedirectAttributes redirectAttributes) {
        Listing listing = listingService.getListingById(id);
        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

        if (bookmarkService.BookmarkExist(loggedInUser, listing)) {
            // Falls das Bookmark bereits existiert, mache nichts und leite zurück
            redirectAttributes.addFlashAttribute("message", "You have already bookmarked this listing.");
            return "redirect:/main";  // Oder zeige eine Nachricht an, dass das Listing schon ein Bookmark ist
        }

        bookmarkService.addBookmark(loggedInUser, listing);

        return "redirect:/main";
    }


    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, HttpSession session) {
        Listing listing = listingRepository.findById(id).get();
        model.addAttribute("listing", listing);

        return "listing-detail";
    }

    @GetMapping("/buy/{id}")
    public String buyListing(@PathVariable Long id, Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

            if (listing.isSold()) {
                return "redirect:/listing/" + id;
            }

            String currency = (String) session.getAttribute("currency");
            String total = String.valueOf(listing.getPrice());

            Payment payment = paypalService.createPayment(
                    "sale",
                    "paypal",
                    currency,
                    total,
                    "Purchase of listing " + id,
                    "http://localhost:8080/listing/cancel",
                    "http://localhost:8080/listing/success?listingId=" + id
            );

            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
            redirectAttributes.addFlashAttribute("error", "Unknown error occurred.");
            return "redirect:/listing/" + id;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment creation failed.");
            return "redirect:/listing/" + id;
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId,
                          @RequestParam("PayerID") String payerId,
                          @RequestParam("listingId") Long listingId,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);

            if (payment.getState().equals("approved")) {
                Listing listing = listingRepository.findById(listingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

                String buyerUsername = principal.getName();
                User buyer = userRepository.findByMail(buyerUsername)
                        .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

                listing.setBuyer(buyer);
                listing.setSold(true);
                listingRepository.save(listing);

                return "success";
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment failed.");
                return "redirect:/listing/" + listingId;
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment execution failed.");
            return "redirect:/listing/" + listingId;
        }
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "cancel";
    }

}
