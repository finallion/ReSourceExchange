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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

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
        LOGGER.info("Attempting to add listing with ID: {} to cart", id);

        try {
            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

            List<Listing> cart = (List<Listing>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }

            if (listing.isSold()) {
                redirectAttributes.addFlashAttribute("error", "Cannot add a sold listing to the cart.");
                LOGGER.warn("Attempted to add a sold listing with ID: {} to the cart", id);
                return "redirect:/main";
            }

            if (cart.stream().anyMatch(l -> l.getId().equals(id))) {
                redirectAttributes.addFlashAttribute("error", "This item is already in your cart.");
                LOGGER.warn("Listing with ID: {} is already in the cart", id);
                return "redirect:/main";
            }

            cart.add(listing);
            redirectAttributes.addFlashAttribute("message", "Listing added to the cart.");
            LOGGER.info("Listing with ID: {} successfully added to the cart", id);

            return "redirect:/main";
        } catch (Exception e) {
            LOGGER.error("Error occurred while adding listing with ID: {} to the cart", id, e);
            return "redirect:/error";
        }
    }


    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to add listing with ID: {} to cart", id);

        try {
            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

            List<Listing> cart = (List<Listing>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }

            if (listing.isSold()) {
                redirectAttributes.addFlashAttribute("error", "Cannot add a sold listing to the cart.");
                LOGGER.warn("Attempted to add a sold listing with ID: {} to the cart", id);
                return "redirect:/listing/" + id;
            }

            if (cart.stream().anyMatch(l -> l.getId().equals(id))) {
                redirectAttributes.addFlashAttribute("error", "This item is already in your cart.");
                LOGGER.warn("Listing with ID: {} is already in the cart", id);
                return "redirect:/listing/" + id;
            }

            cart.add(listing);
            redirectAttributes.addFlashAttribute("message", "Listing added to the cart.");
            LOGGER.info("Listing with ID: {} successfully added to the cart", id);

            return "redirect:/listing/" + id;
        } catch (Exception e) {
            LOGGER.error("Error occurred while adding listing with ID: {} to the cart", id, e);
            return "redirect:/error";
        }
    }


    @GetMapping("/checkout")
    public String checkoutCart(HttpSession session, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to checkout cart");

        try {
            List<Listing> cart = (List<Listing>) session.getAttribute("cart");
            if (cart == null || cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
                LOGGER.warn("Cart is empty, redirecting to main");
                return "redirect:/main";
            }

            double totalPrice = cart.stream().mapToDouble(Listing::getPrice).sum();
            String currency = (String) session.getAttribute("currency");

            if (currency == null) {
                currency = "USD";
            }

            currency = currency.toUpperCase();

            String total = String.format(Locale.US, "%.2f", totalPrice);

            Payment payment = paypalService.createPayment(
                    "sale",
                    "paypal",
                    currency,
                    total,
                    "Purchase of multiple items",
                    "http://localhost:8080/cart/cancel",
                    "http://localhost:8080/cart/success"
            );

            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }

            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred.");
            LOGGER.error("Unexpected error occurred during checkout process");
            return "redirect:/error";
        } catch (PayPalRESTException e) {
            LOGGER.error("Payment creation failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Payment creation failed.");
            return "redirect:/error";
        }
    }


    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId,
                          @RequestParam("PayerID") String payerId,
                          HttpSession session,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            LOGGER.info("Payment success attempt with paymentId: {} and PayerID: {}", paymentId, payerId);
            Payment payment = paypalService.executePayment(paymentId, payerId);

            if (payment.getState().equals("approved")) {
                List<Listing> cart = (List<Listing>) session.getAttribute("cart");
                if (cart == null || cart.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No items in cart to finalize.");
                    LOGGER.warn("Cart is empty, redirecting to main");
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

                LOGGER.info("Payment successful, items purchased, cart cleared");
                return "success";
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment failed.");
                LOGGER.error("Payment not approved, redirecting to main");
                return "redirect:/error";
            }
        } catch (PayPalRESTException e) {
            LOGGER.error("Payment execution failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Payment execution failed.");
            return "redirect:/error";
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred during payment success handling: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
            return "redirect:/error";
        }
    }


    @GetMapping("/cancel")
    public String cancel(RedirectAttributes redirectAttributes) {
        LOGGER.error("Payment was canceled.");
        redirectAttributes.addFlashAttribute("error", "Payment was canceled.");
        return "redirect:/main";
    }
}