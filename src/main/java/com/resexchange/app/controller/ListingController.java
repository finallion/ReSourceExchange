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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Der ListingController ist ein Spring MVC-Controller, der für die Verwaltung von Listings zuständig ist.
 *
 * Diese Klasse enthält alle Endpunkte für die Anzeige, Erstellung, Bearbeitung, Löschung und Interaktion mit Listings.
 * Listings sind Objekte, die auf einer Plattform veröffentlicht werden und Informationen wie Materialien, Preise
 * und Beschreibungen enthalten. Der Controller enthält Methoden zur Anzeige von Formularen, zur Verarbeitung von
 * Formulareingaben sowie zur Verwaltung von Nutzerdaten und Zahlungen.
 *
 * @author Dominik, Lion, Stefan
 */
@Controller
@RequestMapping("/listing")
public class ListingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListingController.class);

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
    @Autowired
    private MaterialService materialService;

    @Autowired
    private MailService mailService;

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um das Formular für ein neues Listing anzuzeigen.
     *
     * @author Dominik
     * @param model das Spring Model, um Attribute an die Ansicht zu übergeben
     * @return der Name der Ansichtsvorlage, die gerendert werden soll ("CreateListings")
     */
    @GetMapping("/create")
    public String showListingForm(Model model) {
        LOGGER.info("Displaying the create listing form");

        model.addAttribute("listing", new Listing());

        // Liste aller vorhandenen Materialien
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);

        LOGGER.info("Loaded {} materials for listing creation", materials.size());

        return "CreateListings";
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um die vom Benutzer getätigten Käufe anzuzeigen.
     *
     * @author Lion
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @param model das Spring Model, um Attribute an die Ansicht zu übergeben
     * @return der Name der Ansichtsvorlage, die gerendert werden soll ("purchases")
     */
    @GetMapping("/purchases")
    public String getUserPurchases(Principal principal, Model model) {
        String mail = principal.getName();
        LOGGER.info("Fetching purchases for user with email: {}", mail);

        List<Listing> purchasedListings = listingRepository.findByBuyerMail(mail);
        LOGGER.info("Found {} purchased listings for user {}", purchasedListings.size(), mail);

        model.addAttribute("purchasedListings", purchasedListings);
        return "purchases";
    }

    /**
     * Controller-Methode zur Behandlung von POST-Anfragen, um ein neues Listing zu speichern.
     *
     * Diese Methode überprüft die Eingabeparameter auf Validität, speichert das Listing und leitet den Benutzer
     * entsprechend weiter, abhängig von der Validität der Eingaben.
     *
     * @author Dominik
     * @param listing das Listing-Objekt, das aus dem Formular übergeben wird
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @param authentication das Authentication-Objekt, das Informationen über den aktuell angemeldeten Benutzer enthält
     * @return eine Redirect-URL, abhängig vom Ergebnis der Validierung und Speicherung
     */
    @PostMapping("/create")
    public String createListing(@ModelAttribute("listing") Listing listing, RedirectAttributes redirectAttributes, Authentication authentication) {
        LOGGER.info("Creating listing with material: {}", listing.getMaterial());

        // Check, ob ein Material ausgewählt wurde
        if (listing.getMaterial() == null || listing.getMaterial().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Material must be selected!");
            LOGGER.warn("Material not selected for listing creation.");
            return "redirect:/listing/create";
        }
        // Check, ob quantity und Preis größer 0
        if (listing.getQuantity() <= 0 || listing.getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity and Price must be greater than 0!");
            LOGGER.warn("Invalid quantity or price for listing creation. Quantity: {}, Price: {}", listing.getQuantity(), listing.getPrice());
            return "redirect:/listing/create";
        }

        User user = userRepository.findByMail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Geokodierte Adresse des Benutzers abrufen
        double[] coordinates = userService.getGeocodedAddressFromUser(user);

        if (coordinates != null) {
            listing.setLatitude(coordinates[0]);
            listing.setLongitude(coordinates[1]);
            LOGGER.info("Geocoded coordinates for user {}: Latitude: {}, Longitude: {}", user.getMail(), coordinates[0], coordinates[1]);
        } else {
            LOGGER.warn("No geocoded address found for user {}", user.getMail());
        }

        listing.setCreatedBy(user);
        listingRepository.save(listing);
        redirectAttributes.addFlashAttribute("success", "Listing successfully created!");
        LOGGER.info("Listing created successfully by user {}", user.getMail());

        return "redirect:/main";
    }


    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um das Formular für die Aktualisierung eines Listings anzuzeigen.
     *
     * Diese Methode überprüft die Berechtigung des angemeldeten Benutzers, lädt das gewünschte Listing
     * und die Liste der Materialien, und stellt die Daten für die Ansicht bereit.
     *
     * @author Dominik
     * @param id die ID des Listings, das aktualisiert werden soll
     * @param initiatorId die ID des Benutzers, der die Aktualisierung initiiert hat
     * @param model das Spring Model, um Attribute an die Ansicht zu übergeben
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @return der Name der Ansichtsvorlage, die gerendert werden soll ("updateListing")
     * @throws IllegalArgumentException wenn der angemeldete Benutzer nicht gefunden wird
     * @throws SecurityException wenn der Benutzer nicht berechtigt ist, die Aktualisierung durchzuführen
     */
    @GetMapping("/update/{id}/{initiatorId}")
    public String showUpdateForm(@PathVariable("id") Long id,
                                 @PathVariable("initiatorId") Long initiatorId,
                                 Model model, Principal principal) {
        LOGGER.info("User {} attempting to update listing with ID: {}", principal.getName(), id);

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

        // Überprüfen, ob der Benutzer berechtigt ist, die Aktualisierung vorzunehmen
        if (!loggedInUser.getId().equals(initiatorId)) {
            LOGGER.warn("Unauthorized access attempt by user {} to update listing with ID: {}", principal.getName(), id);
            throw new SecurityException("Unauthorized access attempt");
        }

        Listing listing = listingService.getListingById(id);
        List<Material> materials = materialRepository.findAll();

        if (listing == null) {
            LOGGER.error("Listing with ID {} not found", id);
            throw new ResourceNotFoundException("Listing not found");
        }

        LOGGER.info("Listing with ID {} found, preparing for update.", id);

        if (listing.getMaterial() != null) {
            LOGGER.info("Listing material is set: {}", listing.getMaterial().getName());
        } else if (!materials.isEmpty()) {
            listing.setMaterial(materials.get(0)); // Setze das erste Material als Standard
            LOGGER.info("No material set for listing, defaulting to first material: {}", materials.get(0).getName());
        }

        model.addAttribute("listing", listing);
        model.addAttribute("materials", materials);

        return "updateListing";
    }


    /**
     * Controller-Methode zur Behandlung von POST-Anfragen, um ein bestehendes Listing zu aktualisieren.
     *
     * Diese Methode überprüft die Berechtigung des angemeldeten Benutzers, aktualisiert die Listing-Daten,
     * und leitet den Benutzer nach erfolgreicher Bearbeitung weiter.
     *
     * @author Dominik
     * @param id die ID des Listings, das aktualisiert werden soll
     * @param initiatorId die ID des Benutzers, der die Aktualisierung initiiert hat
     * @param listing das aktualisierte Listing-Objekt, das aus dem Formular übergeben wird
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @return eine Redirect-URL zur Hauptseite ("redirect:/main")
     * @throws IllegalArgumentException wenn der angemeldete Benutzer nicht gefunden wird
     * @throws SecurityException wenn der Benutzer nicht berechtigt ist, die Aktualisierung durchzuführen
     */
    @PostMapping("/update/{id}/{initiatorId}")
    public String updateListing(@PathVariable("id") Long id,
                                @PathVariable("initiatorId") Long initiatorId,
                                @ModelAttribute Listing listing,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        LOGGER.info("User {} attempting to update listing with ID: {}", principal.getName(), id);

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> {
                    LOGGER.error("Logged-in user with email {} not found", principal.getName());
                    return new IllegalArgumentException("Logged-in user not found");
                });

        // Überprüfen, ob der Benutzer berechtigt ist, die Aktualisierung vorzunehmen
        if (!loggedInUser.getId().equals(initiatorId)) {
            LOGGER.warn("Unauthorized access attempt by user {} to update listing with ID: {}", principal.getName(), id);
            throw new SecurityException("Unauthorized update attempt");
        }

        listing.setId(id);
        listing.setCreatedBy(loggedInUser);
        listing.setChats(listing.getChats());

        // Geokodierte Adresse des Benutzers abrufen und setzen, falls verfügbar
        double[] coordinates = userService.getGeocodedAddressFromUser(loggedInUser);

        if (coordinates != null) {
            listing.setLatitude(coordinates[0]);
            listing.setLongitude(coordinates[1]);
            LOGGER.info("Geocoded coordinates for user {} set: Latitude = {}, Longitude = {}",
                    loggedInUser.getMail(), coordinates[0], coordinates[1]);
        } else {
            LOGGER.warn("Geocoded coordinates for user {} could not be found", loggedInUser.getMail());
        }

        // Das Listing aktualisieren
        listingService.updateListing(listing);

        LOGGER.info("Listing with ID {} successfully updated by user {}", id, principal.getName());

        redirectAttributes.addFlashAttribute("message", "Listing updated successfully.");
        return "redirect:/main";
    }



    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um ein Listing zu löschen.
     *
     * Diese Methode ruft die Löschfunktion des Services auf, um das Listing mit der angegebenen ID zu entfernen,
     * und leitet den Benutzer anschließend auf die Hauptseite weiter.
     *
     * @author Dominik
     * @param id die ID des Listings, das gelöscht werden soll
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @return eine Redirect-URL zur Hauptseite ("redirect:/main")
     */
    @GetMapping("/delete/{id}")
    public String deleteListing(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to delete listing with ID: {}", id);

        try {
            // Löschen des Listings
            listingService.deleteListing(id);
            LOGGER.info("Listing with ID {} successfully deleted", id);

            redirectAttributes.addFlashAttribute("message", "Listing deleted successfully.");
        } catch (Exception e) {
            // Fehlerbehandlung und Logging
            LOGGER.error("Error occurred while trying to delete listing with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete the listing.");
        }

        return "redirect:/main";
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um ein Listing zu den Bookmarks des Benutzers hinzuzufügen.
     *
     * Diese Methode überprüft, ob das Bookmark bereits existiert. Falls nicht, wird es hinzugefügt,
     * und der Benutzer wird anschließend auf die Hauptseite weitergeleitet.
     *
     * @author Dominik
     * @param id die ID des Listings, das gebookmarkt werden soll
     * @param initiatorId die ID des Benutzers, der die Aktion initiiert hat
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @param authentication das Authentication-Objekt, das zusätzliche Authentifizierungsinformationen bereitstellt
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @return eine Redirect-URL zur Hauptseite ("redirect:/main")
     * @throws IllegalArgumentException wenn der angemeldete Benutzer nicht gefunden wird
     */
    @GetMapping("/bookmark/{id}/{initiatorId}")
    public String addBookmark(@PathVariable Long id, @PathVariable Long initiatorId, Principal principal, Authentication authentication, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to bookmark listing with ID: {} by user: {}", id, principal.getName());

        try {
            Listing listing = listingService.getListingById(id);
            User loggedInUser = userRepository.findByMail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

            // Überprüfen, ob das Listing bereits ein Bookmark ist
            if (bookmarkService.BookmarkExist(loggedInUser, listing)) {
                LOGGER.warn("User {} has already bookmarked the listing with ID: {}", principal.getName(), id);
                redirectAttributes.addFlashAttribute("message", "You have already bookmarked this listing.");
                return "redirect:/main";
            }

            // Hinzufügen des Bookmarks
            bookmarkService.addBookmark(loggedInUser, listing);
            LOGGER.info("User {} successfully bookmarked the listing with ID: {}", principal.getName(), id);

        } catch (Exception e) {
            // Fehlerbehandlung und Logging
            LOGGER.error("Error occurred while trying to bookmark listing with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Failed to bookmark the listing.");
        }

        return "redirect:/main";
    }


    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um die Detailansicht eines Listings anzuzeigen.
     *
     * Diese Methode lädt das Listing anhand der angegebenen ID und fügt es dem Modell hinzu,
     * um die Detailansicht der Seite zu rendern.
     *
     * @author Lion
     * @param id die ID des Listings, dessen Details angezeigt werden sollen
     * @param model das Spring Model, um das Listing als Attribut an die Ansicht zu übergeben
     * @param session das HttpSession-Objekt, das Informationen zur Benutzersitzung enthält
     * @return der Name der Ansichtsvorlage, die gerendert werden soll ("listing-detail")
     * @throws NoSuchElementException wenn das Listing mit der angegebenen ID nicht gefunden wird
     */
    @GetMapping("/{id}")
    public String getListing(@PathVariable Long id, Model model, HttpSession session) {
        LOGGER.info("Attempting to retrieve listing with ID: {}", id);

        try {
            // Suchen des Listings anhand der ID
            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Listing with ID " + id + " not found"));

            model.addAttribute("listing", listing);
            LOGGER.info("Successfully retrieved listing with ID: {}", id);

        } catch (NoSuchElementException e) {
            // Fehlerbehandlung und Logging
            LOGGER.error("Listing with ID {} not found", id, e);
            model.addAttribute("error", "Listing not found");
            return "error"; // Redirect or error page in case the listing is not found
        }

        return "listing-detail"; // Anzeige der Detailseite für das Listing
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, um ein Listing zu kaufen.
     *
     * Diese Methode prüft, ob das Listing noch verfügbar ist (nicht verkauft) und leitet den Benutzer dann
     * auf PayPal weiter, um die Zahlung für das Listing zu tätigen. Falls die Zahlung erfolgreich ist,
     * wird der Benutzer zu einer Bestätigungsseite weitergeleitet. Wenn die Zahlung fehlschlägt oder das
     * Listing bereits verkauft ist, wird der Benutzer zurück zur Listing-Detailansicht geleitet.
     *
     * @author Lion
     * @param id die ID des Listings, das gekauft werden soll
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @param session das HttpSession-Objekt, das Informationen zur Benutzersitzung enthält, einschließlich der Währung
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @return eine Redirect-URL zu PayPal oder zu einer Fehler-/Erfolgsseite
     * @throws ResourceNotFoundException wenn das Listing mit der angegebenen ID nicht gefunden wird
     */
    @GetMapping("/buy/{id}")
    public String buyListing(@PathVariable Long id, Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        LOGGER.info("User {} is attempting to purchase listing with ID: {}", principal.getName(), id);

        try {
            // Suchen des Listings anhand der ID
            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

            // Wenn das Listing bereits verkauft wurde
            if (listing.isSold()) {
                LOGGER.info("Listing with ID {} is already sold, redirecting to detail view.", id);
                return "redirect:/listing/" + id;
            }

            String currency = (String) session.getAttribute("currency");
            String total = String.valueOf(listing.getPrice());

            // Erstellen der Zahlung über den PayPal-Service
            Payment payment = paypalService.createPayment(
                    "sale",
                    "paypal",
                    currency,
                    total,
                    "Purchase of listing " + id,
                    "http://localhost:8080/listing/cancel",
                    "http://localhost:8080/listing/success?listingId=" + id
            );

            // Suchen des Links für die PayPal-Zahlungsbestätigung und Weiterleitung
            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    LOGGER.info("Payment created successfully. Redirecting to PayPal approval URL: {}", link.getHref());
                    return "redirect:" + link.getHref();
                }
            }

            LOGGER.error("No approval URL found for payment creation. Redirecting to listing detail page.");
            redirectAttributes.addFlashAttribute("error", "Unknown error occurred.");
            return "redirect:/listing/" + id;

        } catch (PayPalRESTException e) {
            LOGGER.error("Error occurred while creating payment for listing with ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Payment creation failed.");
            return "redirect:/listing/" + id;
        }
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, die nach einer erfolgreichen PayPal-Zahlung aufgerufen wird.
     *
     * Diese Methode prüft, ob die Zahlung erfolgreich war. Wenn die Zahlung genehmigt wurde, wird das Listing
     * als verkauft markiert und der Käufer wird dem Listing zugewiesen. Andernfalls wird der Benutzer auf die
     * Detailansicht des Listings mit einer Fehlermeldung zurückgeleitet.
     *
     * @author Lion
     * @param paymentId die ID der Zahlung, die von PayPal zurückgegeben wird
     * @param payerId die ID des Käufers, der die Zahlung ausgeführt hat
     * @param listingId die ID des Listings, das gekauft wurde
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @param redirectAttributes Objekt zur Übergabe von Flash-Attributen für Weiterleitungen
     * @return der Name der Ansicht, die nach dem Zahlungsergebnis angezeigt wird
     * @throws ResourceNotFoundException wenn das Listing oder der Käufer nicht gefunden wird
     */
    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId,
                          @RequestParam("PayerID") String payerId,
                          @RequestParam("listingId") Long listingId,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {

        LOGGER.info("User {} has successfully completed payment with paymentId: {} for listingId: {}", principal.getName(), paymentId, listingId);

        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);

            // Überprüfen, ob die Zahlung genehmigt wurde
            if (payment.getState().equals("approved")) {
                Listing listing = listingRepository.findById(listingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

                // Den Käufer anhand des aktuellen Benutzernamens (Mail) ermitteln
                String buyerUsername = principal.getName();
                User buyer = userRepository.findByMail(buyerUsername)
                        .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

                // Den Käufer auf das Listing setzen und den Status "verkauft" aktualisieren
                listing.setBuyer(buyer);
                listing.setSold(true);
                listingRepository.save(listing);

                LOGGER.info("Payment successful. Listing with ID {} marked as sold and assigned to buyer {}", listingId, buyerUsername);

                // Bestätigungs-E-Mail an den Käufer senden
                mailService.sendEmail(
                        buyer.getMail(),
                        "Successfully bought at ReSource Exchange!",
                        "You bought: " + listing.getMaterial() + ":" + listing.getQuantity() + " for " + listing.getPrice()
                );

                return "success";
            } else {
                LOGGER.error("Payment failed for paymentId: {} for listingId: {}", paymentId, listingId);
                redirectAttributes.addFlashAttribute("error", "Payment failed.");
                return "redirect:/listing/" + listingId;
            }
        } catch (PayPalRESTException e) {
            LOGGER.error("Error occurred while executing payment for paymentId: {} for listingId: {}: {}", paymentId, listingId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Payment execution failed.");
            return "redirect:/listing/" + listingId;
        }
    }

    /**
     * Controller-Methode zur Behandlung von GET-Anfragen, die aufgerufen werden, wenn eine PayPal-Zahlung abgebrochen wurde.
     *
     * Diese Methode wird verwendet, um den Benutzer auf eine Seite weiterzuleiten, die ihm mitteilt, dass die Zahlung abgebrochen wurde.
     * Sie gibt einfach den Namen der Ansicht zurück, die die entsprechende Nachricht anzeigt.
     *
     * @author Lion
     * @return der Name der Ansicht, die dem Benutzer angezeigt wird, nachdem die Zahlung abgebrochen wurde
     */
    @GetMapping("/cancel")
    public String cancel(Principal principal) {
        LOGGER.info("User {} has canceled the payment process.", principal.getName());
        return "cancel";
    }

    /**
     * @author Stefan
     * @author Dominik
     * @param keyword Das Keyword das in die Suche eingegeben wird
     * @param materialId Die MaterialId welche beim Filtern ausgesucht werden kann
     * @param sold Der Wert der Checkbox "Sold" beim Filtern
     * @param bookmarked Der Wert der Checkbox "Bookmarked" beim Filtern
     * @param minPrice Minimalpreis nach dem gefiltert werden soll
     * @param maxPrice Maximalpreis nach dem gefiltert werden soll
     * @param minQuantity Minimalquantität nach der gefiltert werden soll
     * @param maxQuantity Maximalquantität nach der gefiltert werden soll
     * @param own Der Wert der Checkbox "Own" beim Filtern"
     * @param model das Spring Model, um die Parameter als Attribute an die Ansicht zu übergeben
     * @param principal das Principal-Objekt, das die Authentifizierungsdaten des aktuellen Benutzers enthält
     * @return Ansicht der Mainsite mit gefilterten Listings
     */
    @GetMapping
    public String getFilteredListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) Boolean sold,
            @RequestParam(required = false) Boolean bookmarked,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) Boolean own,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal
    ) {
        int pageIndex = page - 1;
        int pageSize = 8;

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Listing> listings;

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
        Long userId = loggedInUser.getId();
        Long ownedId = userId;

        // Log the received parameters
        LOGGER.info("Filtering listings with parameters: keyword={}, materialId={}, sold={}, bookmarked={}, " +
                        "minPrice={}, maxPrice={}, minQuantity={}, maxQuantity={}, own={}, page={}",
                keyword, materialId, sold, bookmarked, minPrice, maxPrice, minQuantity, maxQuantity, own, page);

        // Wenn ein Suchwort eingegeben ist, soll nach Suchwort gesucht werden, andernfalls werden die Filter angewendet
        if(keyword != null && !keyword.isEmpty()) {
            LOGGER.info("Searching listings with keyword: {}", keyword);
            listings = listingService.getSearchedListings(keyword, pageable);
        } else {
            // Wenn der "Bookmarked" Filter nicht angewendet wird, ist die userId irrelevant und muss null gesetzt werden
            if(bookmarked == null) {
                userId = null;
            }
            // Wenn der "Sold" Filter nicht gesetzt ist, soll nach noch nicht verkauften Listings gesucht werden
            if(sold == null) {
                sold = false;
            }

            if(own != null && own) {
                LOGGER.info("Fetching own listings with filters: materialId={}, sold={}, bookmarked={}, minPrice={}, maxPrice={}",
                        materialId, sold, bookmarked, minPrice, maxPrice);
                listings = listingService.getFilteredListings(materialId, sold, bookmarked, userId, minPrice, maxPrice, minQuantity, maxQuantity, true, ownedId, pageable);
                model.addAttribute("selectedOwn", true);
            } else {
                LOGGER.info("Fetching listings with filters: materialId={}, sold={}, bookmarked={}, minPrice={}, maxPrice={}",
                        materialId, sold, bookmarked, minPrice, maxPrice);
                listings = listingService.getFilteredListings(materialId, sold, bookmarked, userId, minPrice, maxPrice, minQuantity, maxQuantity, false, ownedId, pageable);
                model.addAttribute("selectedOwn", false);
            }

            model.addAttribute("selectedSold", sold != null && sold);
        }

        // Adding materials to the model
        List<Material> materials = materialService.getAllMaterials();

        model.addAttribute("listings", listings.getContent());
        model.addAttribute("materials", materials);
        model.addAttribute("selectedMaterialId", materialId);
        model.addAttribute("selectedBookmarked", bookmarked);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedMinQuantity", minQuantity);
        model.addAttribute("selectedMaxQuantity", maxQuantity);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listings.getTotalPages());
        model.addAttribute("totalElements", listings.getTotalElements());

        // Geolocation of the logged-in user
        double[] coordinates = userService.getGeocodedAddressFromUser(loggedInUser);

        if (coordinates != null) {
            model.addAttribute("userLatitude", coordinates[0]);
            model.addAttribute("userLongitude", coordinates[1]);
        } else {
            // Fallback to Berlin coordinates if geolocation is not available
            model.addAttribute("userLatitude", 52.520008);
            model.addAttribute("userLongitude", 13.404954);
        }

        return "main";
    }
}
