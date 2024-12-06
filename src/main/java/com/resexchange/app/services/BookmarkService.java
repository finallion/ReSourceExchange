package com.resexchange.app.services;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.BookmarkRepository;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
    public class BookmarkService {

        @Autowired
        private BookmarkRepository bookmarkRepository;

        public void addBookmark(User user, Listing listing) {
            Bookmark bookmark = new Bookmark(listing, user);
            bookmark.setUser(user);
            bookmark.setListing(listing);
            bookmarkRepository.save(bookmark);

        }

    public Bookmark findById(Long id) {
        return bookmarkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found with id: " + id));
    }

        public List<Bookmark> getUserBookmarks(User user) {
            return bookmarkRepository.findByUser(user);
        }

    public void deleteBookmark(Long id) {
        if (!bookmarkRepository.existsById(id)) {
            throw new IllegalArgumentException("Bookmark not found with id: " + id);
        }

        bookmarkRepository.deleteById(id);
    }
    public boolean BookmarkExist(User user, Listing listing) {
        return bookmarkRepository.existsByUserAndListing(user, listing);
    }
    }