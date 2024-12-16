package com.resexchange.app.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Der CurrencyController ist für die Verwaltung der Währungen und das Abrufen der API zuständig
 *
 * @author Stefan
 */
@Controller
public class CurrencyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyController.class);
    /**
     * Function to get the desired Currency from the Api and display the new Currency
     */
    @GetMapping("/convert-currency")
    public String convertCurrency(@RequestParam String currency, Model model, HttpSession session) {
        String apiUrl = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.json";
        LOGGER.info("Attempting to convert currency to: {}", currency);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = null;

        try {
            response = restTemplate.getForObject(apiUrl, Map.class);
            LOGGER.info("Successfully retrieved currency data from API.");
        } catch (Exception e) {
            LOGGER.error("Error retrieving currency data from API: {}", e.getMessage());
            session.setAttribute("currency", "eur");
            return "redirect:/main";
        }

        if (response != null) {
            LinkedHashMap<String, Object> eur_to_currency = (LinkedHashMap<String, Object>) response.get("eur");

            if (eur_to_currency != null && eur_to_currency.containsKey(currency)) {
                Double exchange_rate = Double.valueOf(eur_to_currency.get(currency).toString());
                LOGGER.info("Exchange rate for {}: {}", currency, exchange_rate);
                model.addAttribute("exchange_rate", exchange_rate);
                model.addAttribute("currency", currency);
                session.setAttribute("exchange_rate", exchange_rate);
                session.setAttribute("currency", currency);
            } else {
                LOGGER.warn("Currency {} not found in the API response.", currency);
                session.setAttribute("currency", "eur");
            }
        } else {
            LOGGER.error("API response is null.");
            session.setAttribute("currency", "eur");
        }

        return "redirect:/main";
    }

}
