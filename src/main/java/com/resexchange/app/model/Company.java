package com.resexchange.app.model;

import jakarta.persistence.*;

@Entity
public class Company extends User {

    @Column(nullable = false)
    private String companyName;

   // @OneToOne(cascade = CascadeType.ALL)
   // @JoinColumn(name = "address_id", referencedColumnName = "id")
   // private Address address;

    public Company() {}

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String getName() {
        return companyName;
    }
}
