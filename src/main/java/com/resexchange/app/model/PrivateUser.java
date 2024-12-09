package com.resexchange.app.model;

import jakarta.persistence.*;

@Entity
public class PrivateUser extends User {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @Override
    public Address getAddress() {
        return address;
    }
    @Override
    public void setAddress(Address address) {
        this.address = address;
    }

    public PrivateUser() {

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }
}
