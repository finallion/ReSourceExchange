package com.resexchange.app.model;

import jakarta.persistence.*;

/**
 * Modellklasse für eine Benachrichtigung im System.
 * Diese Klasse repräsentiert eine Benachrichtigung, die einem Benutzer zugeordnet ist.
 * Sie enthält Informationen wie die Nachricht, den Status der Benachrichtigung und den zugehörigen Link.
 *
 * @author Dominik
 */
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 255)
    private String message;
    private Boolean checked;
    private String link;

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Notification() {}

    public Notification(String message, User user, Boolean read) {
        this.message = message;
        this.user = user;
        this.checked = read;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

