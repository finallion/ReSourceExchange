package com.resexchange.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestViewController {

    @GetMapping("/")
    public String index() {
        return "test"; // name of the html-doc
    }

}
