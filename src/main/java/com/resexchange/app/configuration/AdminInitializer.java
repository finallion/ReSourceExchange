package com.resexchange.app.configuration;

import com.resexchange.app.model.Address;
import com.resexchange.app.model.Admin;
import com.resexchange.app.model.Permission;
import com.resexchange.app.model.Role;

import com.resexchange.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * Runs on startup to check if an admin user exists.
 * If not, it creates an admin user based on the values in the application.properties file
 */
@Component
public class AdminInitializer implements CommandLineRunner {


    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findUserByMail(adminEmail).isEmpty()) {
            Admin admin = new Admin();
            admin.setMail(adminEmail);
            admin.setVerified(true);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setPermissions(Set.of(
                    Permission.CHAT,
                    Permission.MANAGE_MATERIALS,
                    Permission.MANAGE_LISTINGS
            ));
            Address address = new Address();
            address.setStreet("Seybothstraße 6");
            address.setCity("Regensburg");
            address.setPostalCode("93053");
            address.setCountry("Germany");
            admin.setAddress(address);
            userRepository.save(admin);
        }
    }
}