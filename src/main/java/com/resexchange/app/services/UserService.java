package com.resexchange.app.services;

import com.resexchange.app.model.Admin;
import com.resexchange.app.model.Company;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.Role;
import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        LOGGER.debug("Register company: %s".formatted(company.getPassword()));
        company.setPassword(passwordEncoder.encode(company.getPassword()));
        company.setRole(Role.COMPANY);
        userRepository.save(company);
    }

    public void registerPrivateUser(PrivateUser privateUser) {
        LOGGER.info("Entered raw password: {}", privateUser.getPassword());

        privateUser.setPassword(passwordEncoder.encode(privateUser.getPassword()));

        LOGGER.info("Encoded password saved in the database: {}", privateUser.getPassword());

        privateUser.setRole(Role.PRIVATE_USER);
        userRepository.save(privateUser);
    }

    public void registerAdmin(Admin admin) {
        LOGGER.debug("Register admin user: %s".formatted(admin.getPassword()));
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
