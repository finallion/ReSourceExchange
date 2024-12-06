package com.resexchange.app.configuration;

import com.resexchange.app.model.*;
import com.resexchange.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * Runs on startup to add mockup data for testing the app.
 */
@Component
public class MockupDataInitializer implements CommandLineRunner {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PrivateUserRepository privateUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByMail("max.mustermann@example.com")) {
            PrivateUser privateUser1 = new PrivateUser();
            privateUser1.setFirstName("Max");
            privateUser1.setLastName("Mustermann");
            privateUser1.setMail("max.mustermann@example.com");
            privateUser1.setDisplayName("Max Mustermann");
            privateUser1.setPassword(passwordEncoder.encode("password123"));
            privateUser1.setRole(Role.PRIVATE_USER);
            privateUser1.setPermissions(Set.of(
                    Permission.CHAT,
                    Permission.MANAGE_LISTINGS
            ));
            privateUserRepository.save(privateUser1);
        } else {
            System.out.println("User 'Max Mustermann' already exists.");
        }

        if (!userRepository.existsByMail("erika.musterfrau@example.com")) {
            PrivateUser privateUser2 = new PrivateUser();
            privateUser2.setFirstName("Erika");
            privateUser2.setLastName("Musterfrau");
            privateUser2.setMail("erika.musterfrau@example.com");
            privateUser2.setDisplayName("Erika Musterfrau");
            privateUser2.setPassword(passwordEncoder.encode("password456"));
            privateUser2.setRole(Role.PRIVATE_USER);
            privateUser2.setPermissions(Set.of(
                    Permission.CHAT,
                    Permission.MANAGE_LISTINGS
            ));
            privateUserRepository.save(privateUser2);
        } else {
            System.out.println("User 'Erika Musterfrau' already exists.");
        }

        if (!userRepository.existsByMail("info@techsolutions.com")) {
            Company company1 = new Company();
            company1.setCompanyName("Tech Solutions GmbH");
            company1.setMail("info@techsolutions.com");
            company1.setPassword(passwordEncoder.encode("password789"));
            company1.setRole(Role.COMPANY);
            company1.setPermissions(Set.of(
                    Permission.CHAT,
                    Permission.MANAGE_LISTINGS,
                    Permission.MANAGE_MATERIALS
            ));
            companyRepository.save(company1);
        } else {
            System.out.println("Company 'Tech Solutions GmbH' already exists.");
        }

        if (!userRepository.existsByMail("contact@innodesigns.com")) {
            Company company2 = new Company();
            company2.setCompanyName("Innovative Designs AG");
            company2.setMail("contact@innodesigns.com");
            company2.setPassword(passwordEncoder.encode("password101112"));
            company2.setRole(Role.COMPANY);
            company2.setPermissions(Set.of(
                    Permission.CHAT,
                    Permission.MANAGE_LISTINGS,
                    Permission.MANAGE_MATERIALS
            ));
            companyRepository.save(company2);
        } else {
            System.out.println("Company 'Innovative Designs AG' already exists.");
        }
        // Materials und Listings

        Material material1 = materialRepository.findByName("Holz").orElse(null);
        if (material1 == null) {
            material1 = new Material();
            material1.setName("Holz");
            material1.setDescription("Ich und mein Holz");
            materialRepository.save(material1);
        } else {
            System.out.println("Material 'Holz' already exists.");
        }

        if (!listingRepository.existsByMaterial(material1)) {
            Listing listing1 = new Listing();
            listing1.setMaterial(material1);
            listing1.setPrice(3.99);
            listing1.setQuantity(1);
            listing1.setCreatedBy(userRepository.findById(1));
            listingRepository.save(listing1);
        } else {
            System.out.println("Listing 'Holz' already exists.");
        }

        Material material2 = materialRepository.findByName("Beton").orElse(null);
        if (material2 == null) {
            material2 = new Material();
            material2.setName("Beton");
            material2.setDescription("Toller Beton");
            materialRepository.save(material2);
        } else {
            System.out.println("Material 'Beton' already exists.");
        }

        if (!listingRepository.existsByMaterial(material2)) {
            Listing listing2 = new Listing();
            listing2.setMaterial(material2);
            listing2.setPrice(10.0);
            listing2.setQuantity(5);
            listing2.setCreatedBy(userRepository.findById(2));
            listingRepository.save(listing2);
        } else {
            System.out.println("Listing 'Beton' already exists.");
        }

        Material material3 = materialRepository.findByName("Bildschirm").orElse(null);
        if (material3 == null) {
            material3 = new Material();
            material3.setName("Bildschirm");
            material3.setDescription("Mega Bildschirm");
            materialRepository.save(material3);
        } else {
            System.out.println("Material 'Bildschirm' already exists.");
        }

        if (!listingRepository.existsByMaterial(material3)) {
            Listing listing3 = new Listing();
            listing3.setMaterial(material3);
            listing3.setPrice(50.49);
            listing3.setQuantity(1);
            listing3.setCreatedBy(userRepository.findById(3));
            listingRepository.save(listing3);
        } else {
            System.out.println("Listing 'Bildschirm' already exists.");
        }

    }
}