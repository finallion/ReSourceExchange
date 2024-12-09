package com.resexchange.app.services;

import com.resexchange.app.model.*;
import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerCompanyUser(Company company) {
        Address address = company.getAddress();
        if (address != null) {
            company.setAddress(new Address(address.getStreet(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        company.setPassword(passwordEncoder.encode(company.getPassword()));
        company.setRole(Role.COMPANY);
        company.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(company);
    }

    public void registerPrivateUser(PrivateUser privateUser) {
        Address address = privateUser.getAddress();
        if (address != null) {
            privateUser.setAddress(new Address(address.getStreet(), address.getCity(),
                    address.getPostalCode(), address.getCountry()));
        }
        privateUser.setPassword(passwordEncoder.encode(privateUser.getPassword()));
        privateUser.setRole(Role.PRIVATE_USER);
        privateUser.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(privateUser);
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

    public Address getAddressFromUser(User user) {
        if (user instanceof PrivateUser) {
            return ((PrivateUser) user).getAddress();
        } else if (user instanceof Company) {
            return ((Company) user).getAddress();
        } else if (user instanceof Admin) {
            return ((Admin) user).getAddress();
        } else {
            throw new IllegalArgumentException("Unknown user type: " + user.getClass().getName());
        }
    }
}
