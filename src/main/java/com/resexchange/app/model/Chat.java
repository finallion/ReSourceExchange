package com.resexchange.app.model;

import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import jakarta.persistence.*;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Listing listing;

    @ManyToOne
    private User creator;

    @ManyToOne
    private User initiator;

    public Chat() {

    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }
}