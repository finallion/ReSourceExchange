package com.resexchange.app.controller;

import com.resexchange.app.model.Review;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ReviewRepository;
import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model, Principal principal) {
        User profileUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        User currentUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> {
                    LOGGER.error("Logged-in user not found for email: {}", principal.getName());
                    return new IllegalArgumentException("Logged-in user not found");
                });

        Review existingReview = null;
        if(reviewRepository.findByReviewerAndReviewed(currentUser, profileUser).isPresent()) {
            existingReview = reviewRepository.findByReviewerAndReviewed(currentUser, profileUser).get();
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("existingReview", existingReview != null ? existingReview : new Review());
        model.addAttribute("isNewReview", existingReview == null);
        return "profile";
    }

    @PostMapping("/{userId}/review/create")
    public String createReview(@PathVariable Long userId, @RequestParam int rating, @RequestParam String content, Principal principal) {
        User reviewed = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        User currentUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> {
                    LOGGER.error("Logged-in user not found for email: {}", principal.getName());
                    return new IllegalArgumentException("Logged-in user not found");
                });

        Review review = new Review();
        review.setReviewer(currentUser);
        review.setReviewed(reviewed);
        review.setRating(rating);
        review.setContent(content);

        reviewRepository.save(review);

        return "redirect:/profile/" + userId;
    }

    @PostMapping("/review/edit")
    public String editReview(@RequestParam Long reviewId, @RequestParam int rating, @RequestParam String content) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setRating(rating);
        review.setContent(content);

        reviewRepository.save(review);

        return "redirect:/profile/" + review.getReviewed().getId();
    }

    @GetMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable Long id, Model model) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        Long reviewedId = review.getReviewed().getId();
        reviewRepository.delete(review);
        model.addAttribute("isNewReview", true);

        return "redirect:/profile/" + reviewedId;
    }
}
