package com.resexchange.app.controller;

import com.resexchange.app.model.Company;
import com.resexchange.app.model.Permission;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.CompanyRepository;
import com.resexchange.app.repositories.PrivateUserRepository;
import com.resexchange.app.repositories.UserRepository;
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

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PrivateUserRepository privateUserRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        List<Company> companies = companyRepository.findAll();
        List<PrivateUser> privateUsers = privateUserRepository.findAll();

        List<Object> users = new ArrayList<>();
        users.addAll(companies);
        users.addAll(privateUsers);

        model.addAttribute("users", users);
        return "admin/manage-users";
    }

    @PostMapping("admin/users/{id}/permissions")
    public String updatePermissions(@PathVariable Long id, @RequestParam Set<Permission> permissions) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPermissions(permissions);
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteUserOrCompany(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user instanceof Company) {
            companyRepository.delete((Company) user);
        } else {
            userRepository.delete(user);
        }

        return "redirect:/admin/users";
    }
}