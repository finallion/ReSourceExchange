package com.resexchange.app.model;

import jakarta.persistence.*;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private String country;

    public Address() {}
    public Address(String street, String city, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
