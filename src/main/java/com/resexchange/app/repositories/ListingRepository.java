package com.resexchange.app.repositories;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    // Hier werden alle Listing-Operationen definiert
}
