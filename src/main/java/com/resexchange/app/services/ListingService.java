package com.resexchange.app.services;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MaterialRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse, die verschiedene Operationen für Listings (Angebote) durchführt.
 * Diese Klasse verwaltet das Hinzufügen, Abrufen, Aktualisieren und Löschen von Listings,
 * die mit Materialien verknüpft sind.
 *
 * @author Dominik
 */
@Service
public class ListingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final MaterialRepository materialRepository;

    public ListingService(ListingRepository listingRepository, MaterialRepository materialRepository) {
        this.listingRepository = listingRepository;
        this.materialRepository = materialRepository;
    }


    /**
     * Fügt ein neues Listing hinzu.
     * Wenn das angegebene Material existiert, wird ein neues Listing mit dem angegebenen Material,
     * der Menge und dem Preis erstellt.
     *
     * @param materialId Die ID des Materials, das mit dem Listing verknüpft werden soll
     * @param quantity Die Menge des Materials im Listing
     * @param price Der Preis des Listings
     * @author Dominik
     */
    public void addListing(Long materialId, int quantity, double price) {
        Optional<Material> materialOptional = materialRepository.findById(materialId);

        if (materialOptional.isPresent()) {
            Material material = materialOptional.get();

            Listing listing = new Listing();
            listing.setMaterial(material);  // Verknüpft das Listing mit dem existierenden Material
            listing.setQuantity(quantity);
            listing.setPrice(price);

            LOGGER.info("Listing has been added for material: {}", material.getName());
            listingRepository.save(listing);
        } else {
            LOGGER.warn("Material with ID: {} not found. Listing not created.", materialId);
        }
    }

    /**
     * Ruft alle Listings ab.
     *
     * @return eine Liste von allen Listings
     * @author Dominik
     */
    public List<Listing> getAllListings() {
        LOGGER.info("Retrieving all listings");
        return listingRepository.findAll();
    }

    /**
     * Aktualisiert ein bestehendes Listing.
     * Wenn das Listing existiert, wird es mit den neuen Daten gespeichert.
     *
     * @param listing Das Listing, das aktualisiert werden soll
     * @author Dominik
     */
    public void updateListing(Listing listing) {
        if (listingRepository.existsById(listing.getId())) {
            listingRepository.save(listing);  // Speichert das Listing (update)
        } else {
            LOGGER.warn("Listing with ID: {} not found", listing.getId());
        }
    }

    /**
     * Ruft ein Listing anhand seiner ID ab.
     * Gibt null zurück, wenn das Listing nicht gefunden wird.
     *
     * @param id Die ID des Listings
     * @return Das gefundene Listing oder null, wenn nicht vorhanden
     * @author Dominik
     */
    public Listing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElse(null);  // Gibt null zurück, wenn das Listing nicht gefunden wird
    }


    /**
     * Löscht ein Listing anhand seiner ID.
     * Diese Methode wird in einer Transaktion ausgeführt.
     *
     * @param id Die ID des Listings, das gelöscht werden soll
     * @author Dominik
     */
    @Transactional
    public void deleteListing(Long id) {
        if (listingRepository.existsById(id)) {
            LOGGER.info("Deleting listing with ID: {}", id);
            listingRepository.deleteById(id);
        } else {
            LOGGER.warn("Listing with ID: {} not found", id);
        }
    }

    /**
     * Get Filtered Listings from the Database
     */
    public List<Listing> getFilteredListings(Long materialId, Boolean sold, Boolean bookmarked, Long userId, Double minPrice, Double maxPrice, Integer minQuantity, Integer maxQuantity) {

        return listingRepository.findByFilters(materialId, sold, bookmarked, userId, minPrice, maxPrice, minQuantity, maxQuantity);
    }

    /**
     * Get Searched Listings from the Database
     */
    public List<Listing> getSearchedListings(String keyword) {

        return listingRepository.searchByKeyword(keyword);
    }

}