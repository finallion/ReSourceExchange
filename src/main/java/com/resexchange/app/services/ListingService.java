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

@Service
public class ListingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final MaterialRepository materialRepository;

    public ListingService(ListingRepository listingRepository, MaterialRepository materialRepository) {
        this.listingRepository = listingRepository;
        this.materialRepository = materialRepository;
    }

    // Neuen Listing-Eintrag hinzufügen
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

    // Alle Listings abrufen
    public List<Listing> getAllListings() {
        LOGGER.info("Retrieving all listings");
        return listingRepository.findAll();
    }

    //Listing updaten
    public void updateListing(Listing listing) {
        if (listingRepository.existsById(listing.getId())) {
            listingRepository.save(listing);  // Speichert das Listing (update)
        } else {
            LOGGER.warn("Listing with ID: {} not found", listing.getId());
        }
    }

    public Listing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElse(null);  // Gibt null zurück, wenn das Listing nicht gefunden wird
    }


    // Listing löschen
    @Transactional
    public void deleteListing(Long id) {
        if (listingRepository.existsById(id)) {
            LOGGER.info("Deleting listing with ID: {}", id);
            listingRepository.deleteById(id);
        } else {
            LOGGER.warn("Listing with ID: {} not found", id);
        }
    }
}