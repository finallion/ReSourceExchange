package com.resexchange.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRESTController {
    @GetMapping("/test")
    public String index() {
        return "Hallo from REST!";
    }

}
