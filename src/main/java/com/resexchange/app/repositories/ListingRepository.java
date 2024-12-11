package com.resexchange.app.repositories;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    // Hier werden alle Listing-Operationen definiert
    boolean existsByMaterial(Material material);

    List<Listing> findBySoldFalse();

    List<Listing> findByBuyerMail(String mail);

    /**
     * Query to apply Filters on given Parameters
     */
    @Query("SELECT l FROM Listing l LEFT JOIN Bookmark b ON l.id = b.listing.id WHERE " +
            "(:materialId IS NULL OR l.material.id = :materialId) AND " +
            "(l.sold = :sold) AND " +
            "(:userId IS NULL OR b.user.id = :userId) AND " +
            "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR l.price <= :maxPrice) AND " +
            "(:minQuantity IS NULL OR l.quantity >= :minQuantity) AND " +
            "(:maxQuantity IS NULL OR l.quantity <= :maxQuantity)")
    List<Listing> findByFilters(@Param("materialId") Long materialId,
                                @Param("sold") Boolean sold,
                                @Param("bookmarked") Boolean bookmarked,
                                @Param("userId") Long userId,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice,
                                @Param("minQuantity") Integer minQuantity,
                                @Param("maxQuantity") Integer maxQuantity
    );

    /**
     * Query for the Search function -> Checks if keyword is contained in attributes and returns accordingly
     */
    @Query("SELECT l FROM Listing l " +
            "LEFT JOIN User u on l.createdBy.id = u.id  " +
            "LEFT JOIN PrivateUser pu ON u.id = pu.id " +
            "LEFT JOIN Company c ON u.id = c.id " +
            "WHERE LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.material.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(pu.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(pu.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Listing> searchByKeyword(@Param("keyword") String keyword);
}
