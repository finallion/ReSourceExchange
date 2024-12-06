package com.resexchange.app.repositories;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndListing(User user, Listing listing);

    void deleteByUserAndListing(User user, Listing listing);

    List<Bookmark> findByUser(User user);
    List<Bookmark> findByUserId(Long userId);
}
