package com.resexchange.app.model;

import jakarta.persistence.Entity;

import java.util.Set;

@Entity
public class Admin extends User {

    public Admin() {

    }

    @Override
    public String getName() {
        return "Admin";
    }
}
