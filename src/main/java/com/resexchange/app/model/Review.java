package com.resexchange.app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating; // Bewertungsskala, z.B. 1 bis 5

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonManagedReference
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "reviewed_id", nullable = false)
    @JsonManagedReference
    private User reviewed;

    public Review() {
    }

    public Review(User reviewer, User reviewed, int rating, String content) {
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.rating = rating;
        this.content = content;
    }


    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReviewed() {
        return reviewed;
    }

    public void setReviewed(User reviewed) {
        this.reviewed = reviewed;
    }
}
