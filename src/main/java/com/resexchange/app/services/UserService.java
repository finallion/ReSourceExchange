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
        company.setPassword(passwordEncoder.encode(company.getPassword()));
        company.setRole(Role.COMPANY);
        company.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(company);
    }

    public void registerPrivateUser(PrivateUser privateUser) {
        privateUser.setPassword(passwordEncoder.encode(privateUser.getPassword()));
        privateUser.setRole(Role.PRIVATE_USER);
        privateUser.setPermissions(Set.of(Permission.CHAT, Permission.MANAGE_LISTINGS));
        userRepository.save(privateUser);
    }

    public void registerAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
