package com.resexchange.app.model;

import jakarta.persistence.*;

@Entity
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Primärschlüssel für Bookmark

    @ManyToOne
    @JoinColumn(name = "listing_id")  // Fremdschlüssel zur Tabelle "Listing"
    private Listing listing;

    @ManyToOne
    @JoinColumn(name = "user_id")  // Fremdschlüssel zur Tabelle "User"
    private User user;

    // Standardkonstruktor (wird von JPA benötigt)
    public Bookmark() {}

    // Konstruktor mit Parametern
    public Bookmark(Listing listing, User owner) {
        this.listing = listing;
        this.user = owner;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
