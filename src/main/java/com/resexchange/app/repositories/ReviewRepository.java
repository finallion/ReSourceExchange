package com.resexchange.app.repositories;

import com.resexchange.app.model.Review;
import com.resexchange.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByReviewerAndReviewed(User reviewer, User reviewed);
}