package com.resexchange.app.controller;

import jakarta.servlet.http.HttpSession;
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

    /**
     * Function to get the desired Currency from the Api and display the new Currency
     */
    @GetMapping("/convert-currency")
    public String convertCurrency(@RequestParam String currency, Model model, HttpSession session) {
        /** API: https://github.com/fawazahmed0/exchange-api **/
        String apiUrl = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.json";

        RestTemplate restTemplate = new RestTemplate();
        /**Get the JSON from the API**/
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);

        if (response != null) {
            /** Convert the JSON to a Map **/
            LinkedHashMap<String, Object> eur_to_currency = (LinkedHashMap<String, Object>) response.get("eur");
            /** Get the euro to currency exchange rate and add it **/
            Double exchange_rate =  Double.valueOf(eur_to_currency.get(currency).toString());
            model.addAttribute("exchange_rate", exchange_rate);
            model.addAttribute("currency", currency);
            session.setAttribute("exchange_rate", exchange_rate);
            session.setAttribute("currency", currency);
        } else {
            session.setAttribute("currency", "eur");
        }
        return "redirect:/main";
    }
}
