package com.resexchange.app.services;

import com.resexchange.app.model.Listing;
import com.resexchange.app.repositories.ListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Aktualisiert ein bestehendes Listing.
     *
     * Wenn das Listing existiert, wird es mit den neuen Daten gespeichert. Falls das Listing nicht gefunden wird,
     * wird eine Warnung protokolliert, aber keine Änderungen vorgenommen.
     *
     * @param listing Das Listing, das aktualisiert werden soll. Das Listing muss eine gültige ID enthalten.
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
     *
     * Diese Methode durchsucht die Datenbank nach einem Listing mit der angegebenen ID.
     * Wenn das Listing nicht gefunden wird, gibt die Methode `null` zurück.
     *
     * @param id Die ID des Listings
     * @return Das gefundene Listing oder `null`, wenn kein Listing mit der angegebenen ID vorhanden ist
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
     *
     * Diese Methode löscht ein Listing mit der angegebenen ID. Sie wird in einer Transaktion ausgeführt,
     * was bedeutet, dass bei einem Fehler alle Änderungen rückgängig gemacht werden.
     * Falls das Listing nicht existiert, wird eine Warnung im Log ausgegeben.
     *
     * @param id Die ID des Listings, das gelöscht werden soll
     * @throws RuntimeException Wenn ein Fehler beim Löschen des Listings auftritt
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
     * Ruft gefilterte Listings aus der Datenbank ab, basierend auf den angegebenen Filterparametern.
     *
     * Diese Methode verwendet eine Vielzahl von Parametern, um Listings zu filtern, einschließlich Material-ID, Verkaufsstatus,
     * Buchmarkierungsstatus, Benutzer-ID und Preisspanne. Sie gibt eine Seite von Listings zurück, die den Filterkriterien entsprechen.
     *
     * @param materialId Die ID des Materials, nach dem gefiltert werden soll (optional).
     * @param sold Der Verkaufsstatus der Listings (optional).
     * @param bookmarked Der Buchmarkierungsstatus der Listings (optional).
     * @param userId Die ID des Benutzers, für den die Listings angezeigt werden sollen (optional).
     * @param minPrice Der minimale Preis, nach dem gefiltert werden soll (optional).
     * @param maxPrice Der maximale Preis, nach dem gefiltert werden soll (optional).
     * @param minQuantity Die minimale Menge, nach der gefiltert werden soll (optional).
     * @param maxQuantity Die maximale Menge, nach der gefiltert werden soll (optional).
     * @param own Gibt an, ob nur eigene Listings angezeigt werden sollen (optional).
     * @param ownedId Die ID des Benutzers, dessen Listings angezeigt werden sollen, wenn `own=true` (optional).
     * @param pageable Die Paginierungsinformationen, die die Seitengröße und die Seite definieren.
     * @return Eine Seite von Listings, die den Filterkriterien entsprechen.
     * @throws RuntimeException Wenn ein Fehler beim Abrufen der Listings auftritt.
     * @author Stefan
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
     * Sucht Listings aus der Datenbank, die mit einem angegebenen Schlüsselwort übereinstimmen.
     *
     * Diese Methode verwendet das angegebene Schlüsselwort, um in der Datenbank nach Listings zu suchen.
     * Es wird eine Seite von Listings zurückgegeben, die das Schlüsselwort enthalten.
     *
     * @param keyword Das Suchwort, nach dem in den Listings gesucht werden soll.
     * @param pageable Die Paginierungsinformationen, die die Seitengröße und die Seite definieren.
     * @return Eine Seite von Listings, die dem Suchkriterium entsprechen.
     * @throws RuntimeException Wenn ein Fehler bei der Suche nach Listings auftritt.
     * @author Stefan
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
}