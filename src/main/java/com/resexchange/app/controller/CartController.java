package com.resexchange.app.controller;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.MailService;
import com.resexchange.app.services.PaypalService;
import com.resexchange.app.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaypalService paypalService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;


    @GetMapping("/addFromMain/{id}")
    public String addToCartFromMain(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        List<Listing> cart = (List<Listing>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        if (listing.isSold()) {
            redirectAttributes.addFlashAttribute("error", "Cannot add a sold listing to the cart.");
            return "redirect:/main";
        }

        if (cart.stream().anyMatch(l -> l.getId().equals(id))) {
            redirectAttributes.addFlashAttribute("error", "This item is already in your cart.");
            return "redirect:/main";
        }

        cart.add(listing);
        redirectAttributes.addFlashAttribute("message", "Listing added to the cart.");
        return "redirect:/main";
    }

    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        List<Listing> cart = (List<Listing>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        if (listing.isSold()) {
            redirectAttributes.addFlashAttribute("error", "Cannot add a sold listing to the cart.");
            return "redirect:/listing/" + id;
        }

        if (cart.stream().anyMatch(l -> l.getId().equals(id))) {
            redirectAttributes.addFlashAttribute("error", "This item is already in your cart.");
            return "redirect:/listing/" + id;
        }

        cart.add(listing);
        redirectAttributes.addFlashAttribute("message", "Listing added to the cart.");
        return "redirect:/listing/" + id;
    }

    @GetMapping("/checkout")
    public String checkoutCart(HttpSession session, RedirectAttributes redirectAttributes) {
        List<Listing> cart = (List<Listing>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/main";
        }

        double totalPrice = cart.stream().mapToDouble(Listing::getPrice).sum();

        String currency = (String) session.getAttribute("currency");

        if (currency == null) {
            currency = "USD";
        }

        currency = currency.toUpperCase();

        String total = String.format(Locale.US, "%.2f", totalPrice);

        try {
            Payment payment = paypalService.createPayment(
                    "sale",
                    "paypal",
                    currency,
                    total,
                    "Purchase of multiple items",
                    "http://localhost:8080/cart/cancel",
                    "http://localhost:8080/cart/success"
            );

            // Find approval_url and redirect
            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }

            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred.");
            return "redirect:/main";
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment creation failed.");
            return "redirect:/main";
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId,
                          @RequestParam("PayerID") String payerId,
                          HttpSession session,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                List<Listing> cart = (List<Listing>) session.getAttribute("cart");
                if (cart == null || cart.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No items in cart to finalize.");
                    return "redirect:/main";
                }

                double totalPrice = cart.stream().mapToDouble(Listing::getPrice).sum();

                String buyerUsername = principal.getName();
                User buyer = userRepository.findByMail(buyerUsername)
                        .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

                for (Listing listing : cart) {
                    listing.setBuyer(buyer);
                    listing.setSold(true);
                    listingRepository.save(listing);
                }

                session.removeAttribute("cart");

                mailService.sendEmail(
                        buyer.getMail(),
                        "Successfully bought at ReSource Exchange!",
                        "You bought: " + cart + " for " + totalPrice
                );

                return "success";
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment failed.");
                return "redirect:/main";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment execution failed.");
            return "redirect:/main";
        }
    }

    @GetMapping("/cancel")
    public String cancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Payment was canceled.");
        return "redirect:/main";
    }
}