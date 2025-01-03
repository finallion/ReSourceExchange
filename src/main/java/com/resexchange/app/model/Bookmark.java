package com.resexchange.app.model;

import jakarta.persistence.*;

/**
 * Die Bookmark-Entität stellt ein Lesezeichen dar, das ein Benutzer für ein bestimmtes Listing gesetzt hat.
 *
 * Diese Klasse wird in der Datenbank gespeichert und enthält die Informationen über das Lesezeichen,
 * wie das Listing, das mit dem Lesezeichen verknüpft ist, sowie den Benutzer, der das Lesezeichen gesetzt hat.
 *
 * Jedes Lesezeichen verweist auf ein Listing und einen Benutzer, sodass das Lesezeichen eindeutig einem Benutzer
 * und einem bestimmten Listing zugeordnet ist.
 *
 * @author Dominik
 */
@Entity
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Standardkonstruktor
    public Bookmark() {}

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
