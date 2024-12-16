package com.resexchange.app.controller;

import com.resexchange.app.model.Company;
import com.resexchange.app.model.Permission;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.CompanyRepository;
import com.resexchange.app.repositories.PrivateUserRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.BookmarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class AdminController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PrivateUserRepository privateUserRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        LOGGER.info("Retrieving all users for management");

        try {
            List<Company> companies = companyRepository.findAll();
            List<PrivateUser> privateUsers = privateUserRepository.findAll();

            LOGGER.info("Found {} companies and {} private users", companies.size(), privateUsers.size());

            List<Object> users = new ArrayList<>();
            users.addAll(companies);
            users.addAll(privateUsers);

            model.addAttribute("users", users);
            LOGGER.info("Successfully added users to model for display");

            return "admin/manage-users";
        } catch (Exception e) {
            LOGGER.error("Error occurred while managing users", e);
            throw new RuntimeException("Error occurred while managing users", e);
        }
    }


    @PostMapping("admin/users/{id}/permissions")
    public String updatePermissions(@PathVariable Long id, @RequestParam Set<Permission> permissions) {
        LOGGER.info("Updating permissions for user with ID: {}", id);

        try {
            User user = userRepository
                    .findById(id)
                    .orElseThrow(() -> {
                        LOGGER.error("User with ID: {} not found", id);
                        return new IllegalArgumentException("User not found");
                    });

            LOGGER.info("User found: {}. Current permissions: {}", id, user.getPermissions());

            user.setPermissions(permissions);
            userRepository.save(user);

            LOGGER.info("Permissions for user with ID: {} updated successfully", id);

            return "redirect:/admin/users";
        } catch (Exception e) {
            LOGGER.error("Error updating permissions for user with ID: {}", id, e);
            throw new RuntimeException("Error updating permissions", e);
        }
    }


    @PostMapping("/admin/delete/{id}")
    public String deleteUserOrCompany(@PathVariable Long id) {
        LOGGER.info("Attempting to delete user or company with ID: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        LOGGER.error("User with ID: {} not found", id);
                        return new IllegalArgumentException("User not found");
                    });

            if (user instanceof Company) {
                companyRepository.delete((Company) user);
                LOGGER.info("Company with ID: {} deleted successfully", id);
            } else {
                userRepository.delete(user);
                LOGGER.info("User with ID: {} deleted successfully", id);
            }

            return "redirect:/admin/users";
        } catch (Exception e) {
            LOGGER.error("Error occurred while deleting user or company with ID: {}", id, e);
            throw new RuntimeException("Error occurred while deleting user or company", e);
        }
    }

}