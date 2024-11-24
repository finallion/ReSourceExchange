package com.resexchange.app.configuration;

import com.resexchange.app.model.*;
import com.resexchange.app.repositories.CompanyRepository;
import com.resexchange.app.repositories.PrivateUserRepository;
import com.resexchange.app.repositories.UserRepository;
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
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByMail("max.mustermann@example.com")) {
            PrivateUser privateUser1 = new PrivateUser();
            privateUser1.setFirstName("Max");
            privateUser1.setLastName("Mustermann");
            privateUser1.setMail("max.mustermann@example.com");
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
    }
}