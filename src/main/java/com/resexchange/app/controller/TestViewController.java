package com.resexchange.app.controller;

import com.resexchange.app.repositories.PrivateUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestViewController {

    @Autowired
    private PrivateUserRepository privateUserRepository;

    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("users", privateUserRepository.findByName("Lion"));
        return "test"; // name of the html-doc
    }

}
