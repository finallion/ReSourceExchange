package com.resexchange.app.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Admin extends User {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    public Admin() {

    }

    @Override
    public String getName() {
        return "Admin";
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public void setAddress(Address address) {
        this.address = address;
    }
}
