package com.resexchange.app.services;

import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.Review;
import com.resexchange.app.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }
}
