package com.resexchange.app.services;

import com.resexchange.app.model.*;
import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void registerCompanyUser(Company company) {
        LOGGER.info("Registering new company user: {}", company.getMail());

        try {
            Address address = company.getAddress();
            if (address != null) {
                company.setAddress(new Address(address.getStreet(), address.getCity(),
                        address.getPostalCode(), address.getCountry()));
                LOGGER.info("Address for company {} set successfully", company.getMail());
            }

            company.setPassword(passwordEncoder.encode(company.getPassword()));
            LOGGER.info("Password for company {} has been encoded", company.getMail());

            company.setVerified(false);
            String token = UUID.randomUUID().toString();
            company.setVerificationToken(token);
            LOGGER.info("Generated verification token for company {}: {}", company.getMail(), token);

            company.setRole(Role.COMPANY);
            company.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
            LOGGER.info("Set role and permissions for company {}: {} - {}", company.getMail(), Role.COMPANY, Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));

            userRepository.save(company);
            LOGGER.info("Company user {} saved successfully", company.getMail());

            String link = "http://localhost:8080/verify?token=" + token;
            mailService.sendEmail(
                    company.getMail(),
                    "Email Verification",
                    "Click the following link to verify your email: " + link
            );
            LOGGER.info("Verification email sent to company {}", company.getMail());
        } catch (Exception e) {
            LOGGER.error("Error occurred while registering company user: {}", company.getMail(), e);
            throw new RuntimeException("Error occurred while registering company user", e); // Fehler weiterwerfen
        }
    }


    public void registerPrivateUser(PrivateUser privateUser) {
        LOGGER.info("Registering new private user: {}", privateUser.getMail());

        try {
            Address address = privateUser.getAddress();
            if (address != null) {
                privateUser.setAddress(new Address(address.getStreet(), address.getCity(),
                        address.getPostalCode(), address.getCountry()));
                LOGGER.info("Address for private user {} set successfully", privateUser.getMail());
            }

            privateUser.setPassword(passwordEncoder.encode(privateUser.getPassword()));
            LOGGER.info("Password for private user {} has been encoded", privateUser.getMail());

            privateUser.setVerified(false);
            String token = UUID.randomUUID().toString();
            privateUser.setVerificationToken(token);
            LOGGER.info("Generated verification token for private user {}: {}", privateUser.getMail(), token);

            privateUser.setRole(Role.PRIVATE_USER);
            privateUser.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
            LOGGER.info("Set role and permissions for private user {}: {} - {}", privateUser.getMail(), Role.PRIVATE_USER, Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));

            userRepository.save(privateUser);
            LOGGER.info("Private user {} saved successfully", privateUser.getMail());

            String link = "http://localhost:8080/verify?token=" + token;
            mailService.sendEmail(
                    privateUser.getMail(),
                    "Email Verification",
                    "Click the following link to verify your email: " + link
            );
            LOGGER.info("Verification email sent to private user {}", privateUser.getMail());

        } catch (Exception e) {
            LOGGER.error("Error occurred while registering private user: {}", privateUser.getMail(), e);
            throw new RuntimeException("Error occurred while registering private user", e); // Fehler weiterwerfen
        }
    }


    public void verifyUser(String token) {
        LOGGER.info("Verifying user with token: {}", token);

        try {
            User user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

            LOGGER.info("User with token: {} found, verifying user", token);

            user.setVerified(true);
            user.setVerificationToken(null);

            userRepository.save(user);
            LOGGER.info("User with token: {} has been successfully verified", token);

        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid token provided for verification: {}", token, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while verifying user with token: {}", token, e);
            throw new RuntimeException("Unexpected error occurred while verifying user", e);
        }
    }


    public void registerAdmin(Admin admin) {
        LOGGER.info("Registering new admin: {}", admin.getMail());

        try {
            Address address = admin.getAddress();
            if (address != null) {
                admin.setAddress(new Address(address.getStreet(), address.getCity(),
                        address.getPostalCode(), address.getCountry()));
                LOGGER.info("Address for admin {} set successfully", admin.getMail());
            }

            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            LOGGER.info("Password for admin {} has been encoded", admin.getMail());

            admin.setRole(Role.ADMIN);
            LOGGER.info("Set role for admin {}: {}", admin.getMail(), Role.ADMIN);

            userRepository.save(admin);
            LOGGER.info("Admin {} saved successfully", admin.getMail());
        } catch (Exception e) {
            LOGGER.error("Error occurred while registering admin: {}", admin.getMail(), e);
            throw new RuntimeException("Error occurred while registering admin", e);
        }
    }


    public double[] getGeocodedAddressFromUser(User user) {
        LOGGER.info("Geocoding address for user: {}", user.getMail());

        Address address = null;
        try {
            if (user instanceof PrivateUser) {
                address = ((PrivateUser) user).getAddress();
            } else if (user instanceof Company) {
                address = ((Company) user).getAddress();
            } else if (user instanceof Admin) {
                address = ((Admin) user).getAddress();
            } else {
                LOGGER.error("Unknown user type: {}", user.getClass().getName());
                throw new IllegalArgumentException("Unknown user type: " + user.getClass().getName());
            }

            if (address == null) {
                LOGGER.warn("No address found for user: {}", user.getMail());
                throw new IllegalArgumentException("User does not have an address");
            }

            LOGGER.info("Address for user {}: {} {}, {} {}",
                    user.getMail(),
                    address.getStreet(),
                    address.getCity(),
                    address.getPostalCode(),
                    address.getCountry());

            return GeocodingService.getCoordinatesFromAddress(address.getStreet(), address.getCity(), address.getPostalCode(), address.getCountry());
        } catch (Exception e) {
            LOGGER.error("Error geocoding address for user: {}", user.getMail(), e);
            throw new RuntimeException("Error geocoding address", e);
        }
    }

}
