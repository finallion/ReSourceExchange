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
        Address address = company.getAddress();
        if (address != null) {
            company.setAddress(new Address(address.getStreet(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        company.setPassword(passwordEncoder.encode(company.getPassword()));
        company.setVerified(false);
        String token = UUID.randomUUID().toString();
        company.setVerificationToken(token);
        company.setRole(Role.COMPANY);
        company.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(company);

        String link = "http://localhost:8080/verify?token=" + token;
        mailService.sendEmail(
                company.getMail(),
                "Email Verification",
                "Click the following link to verify your email: " + link
        );
    }

    public void registerPrivateUser(PrivateUser privateUser) {
        Address address = privateUser.getAddress();
        if (address != null) {
            privateUser.setAddress(new Address(address.getStreet(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        privateUser.setPassword(passwordEncoder.encode(privateUser.getPassword()));
        privateUser.setVerified(false);
        String token = UUID.randomUUID().toString();
        privateUser.setVerificationToken(token);
        privateUser.setRole(Role.PRIVATE_USER);
        privateUser.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(privateUser);

        String link = "http://localhost:8080/verify?token=" + token;
        mailService.sendEmail(
                privateUser.getMail(),
                "Email Verification",
                "Click the following link to verify your email: " + link
        );
    }

    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void registerAdmin(Admin admin) {
        Address address = admin.getAddress();
        if (address != null) {
            admin.setAddress(new Address(address.getStreet(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    public double[] getGeocodedAddressFromUser(User user) {
        Address address = null;
        if (user instanceof PrivateUser) {
            address = ((PrivateUser) user).getAddress();
        }
        else if (user instanceof Company) {
             address = ((Company) user).getAddress();
        }
        else if (user instanceof Admin) {
             address = ((Admin) user).getAddress();
        }
        else {
            throw new IllegalArgumentException("Unknown user type: " + user.getClass().getName());
        }
        return GeocodingService.getCoordinatesFromAddress(address.getStreet(), address.getCity(), address.getPostalCode(), address.getCountry());
    }
}
