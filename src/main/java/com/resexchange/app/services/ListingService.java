package com.resexchange.app.services;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
     * Ruft alle Listings ab.
     *
     * @return eine Liste von allen Listings
     * @author Dominik
     */
    public List<Listing> getAllListings() {
        LOGGER.info("Retrieving all listings from the database");

        try {
            List<Listing> listings = listingRepository.findAll();
            LOGGER.info("Successfully retrieved {} listings", listings.size());
            return listings;
        } catch (Exception e) {
            LOGGER.error("Error occurred while retrieving listings", e);
            throw new RuntimeException("Error occurred while retrieving listings", e); // Optional: Exception weiterwerfen
        }
    }

    /**
     * Aktualisiert ein bestehendes Listing.
     * Wenn das Listing existiert, wird es mit den neuen Daten gespeichert.
     *
     * @param listing Das Listing, das aktualisiert werden soll
     * @author Dominik
     */
    public void updateListing(Listing listing) {
        LOGGER.info("Attempting to update listing with ID: {}", listing.getId());

        if (listingRepository.existsById(listing.getId())) {
            try {
                listingRepository.save(listing);
                LOGGER.info("Successfully updated listing with ID: {}", listing.getId());
            } catch (Exception e) {
                LOGGER.error("Error occurred while updating listing with ID: {}", listing.getId(), e);
                throw new RuntimeException("Error occurred while updating listing with ID: " + listing.getId(), e);
            }
        } else {
            LOGGER.warn("Listing with ID: {} not found, cannot update", listing.getId());
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
        LOGGER.info("Retrieving listing with ID: {}", id);

        Listing listing = listingRepository.findById(id).orElse(null);

        if (listing != null) {
            LOGGER.info("Successfully retrieved listing with ID: {}", id);
        } else {
            LOGGER.warn("No listing found with ID: {}", id);
        }

        return listing;
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
        LOGGER.info("Attempting to delete listing with ID: {}", id);

        if (listingRepository.existsById(id)) {
            try {
                listingRepository.deleteById(id);
                LOGGER.info("Successfully deleted listing with ID: {}", id);
            } catch (Exception e) {
                LOGGER.error("Error occurred while deleting listing with ID: {}", id, e);
                throw new RuntimeException("Error occurred while deleting listing with ID: " + id, e); // Optional: Exception weiterwerfen
            }
        } else {
            LOGGER.warn("Listing with ID: {} not found, cannot delete", id);
        }
    }

    /**
     * Get Filtered Listings from the Database
     */
    public Page<Listing> getFilteredListings(Long materialId, Boolean sold, Boolean bookmarked, Long userId, Double minPrice, Double maxPrice, Integer minQuantity, Integer maxQuantity, Boolean own, Long ownedId, Pageable pageable) {
        LOGGER.info("Attempting to retrieve filtered listings with parameters: materialId={}, sold={}, bookmarked={}, userId={}, minPrice={}, maxPrice={}, minQuantity={}, maxQuantity={}, own={}, ownedId={}",
                materialId, sold, bookmarked, userId, minPrice, maxPrice, minQuantity, maxQuantity, own, ownedId);

        try {
            Page<Listing> listings = listingRepository.findByFilters(materialId, sold, bookmarked, userId, minPrice, maxPrice, minQuantity, maxQuantity, own, ownedId, pageable);

            LOGGER.info("Successfully retrieved {} listings with applied filters", listings.getTotalElements());

            return listings;
        } catch (Exception e) {
            LOGGER.error("Error occurred while retrieving filtered listings", e);
            throw new RuntimeException("Error occurred while retrieving filtered listings", e); // Optional: Exception weiterwerfen
        }
    }

    /**
     * Get Searched Listings from the Database
     */
    public Page<Listing> getSearchedListings(String keyword, Pageable pageable) {
        LOGGER.info("Searching for listings with keyword: {}", keyword);

        try {
            Page<Listing> listings = listingRepository.searchByKeyword(keyword, pageable);

            LOGGER.info("Successfully retrieved {} listings for keyword: {}", listings.getTotalElements(), keyword);

            return listings;
        } catch (Exception e) {
            LOGGER.error("Error occurred while searching for listings with keyword: {}", keyword, e);
            throw new RuntimeException("Error occurred while searching for listings with keyword: " + keyword, e); // Optional: Exception weiterwerfen
        }
    }

    /**
     * Hilfsmethode um Anzahl der Pages zu berechnen
     * @param pageSize Anzahl der Listings die auf einer Seite dargestellt werden sollen
     * @return Berechnete Anzahl an Seiten
     */
    public int getTotalPages(int pageSize) {
        LOGGER.info("Calculating total pages for page size: {}", pageSize);

        try {
            long totalListings = listingRepository.count();
            int totalPages = (int) Math.ceil((double) totalListings / pageSize);

            LOGGER.info("Total listings: {}. Calculated total pages: {}", totalListings, totalPages);

            return totalPages;
        } catch (Exception e) {
            LOGGER.error("Error occurred while calculating total pages", e);
            throw new RuntimeException("Error occurred while calculating total pages", e); // Optional: Exception weiterwerfen
        }
    }

}