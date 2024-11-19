package com.resexchange.app.model;

import jakarta.persistence.*;

@Entity
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Eine Beziehung zu einem bereits existierenden Material
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private int quantity;  // Anzahl der verfügbaren Materialien in diesem Listing
    private double price;  // Preis für das Material in diesem Listing

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
