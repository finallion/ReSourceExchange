package com.resexchange.app.services;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaypalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaypalService.class);

    @Autowired
    private APIContext apiContext;

    /**
     * Create a PayPal payment
     */
    public Payment createPayment(String intent, String payerId, String currency,
                                 String total, String description,
                                 String cancelUrl, String successUrl) throws PayPalRESTException {
        LOGGER.info("Creating payment with intent: {}, payerId: {}, currency: {}, total: {}, description: {}",
                intent, payerId, currency, total, description);

        try {
            // Initialisiere das Payment-Objekt
            Payment payment = new Payment();

            // Setze Payment-Details
            payment.setIntent(intent);
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            payment.setPayer(payer);

            // Setze Redirect-URLs
            payment.setRedirectUrls(new com.paypal.api.payments.RedirectUrls()
                    .setCancelUrl(cancelUrl)
                    .setReturnUrl(successUrl));

            String uppercaseCurrency = currency.toUpperCase();

            // Setze Amount und Transaction
            Amount amount = new Amount()
                    .setCurrency(uppercaseCurrency)
                    .setTotal(total);

            Transaction transaction = (Transaction) new Transaction()
                    .setDescription(description)
                    .setAmount(amount);

            payment.setTransactions(List.of(transaction));

            // API-Aufruf zum Erstellen der Zahlung
            LOGGER.info("Sending request to PayPal API to create payment...");
            Payment createdPayment = payment.create(apiContext);

            LOGGER.info("Payment created successfully with ID: {}", createdPayment.getId());

            return createdPayment;
        } catch (PayPalRESTException e) {
            LOGGER.error("Error occurred while creating payment with intent: {}", intent, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while creating payment", e);
            throw new RuntimeException("Unexpected error occurred while creating payment", e);
        }
    }

    /**
     * Execute a PayPal payment
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        LOGGER.info("Attempting to execute payment with paymentId: {} and payerId: {}", paymentId, payerId);

        try {
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecute = new PaymentExecution();
            paymentExecute.setPayerId(payerId);

            LOGGER.info("Sending request to PayPal API to execute payment...");
            Payment executedPayment = payment.execute(apiContext, paymentExecute);

            LOGGER.info("Payment executed successfully with ID: {}", executedPayment.getId());
            return executedPayment;

        } catch (PayPalRESTException e) {
            LOGGER.error("Error occurred while executing payment with paymentId: {} and payerId: {}", paymentId, payerId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while executing payment", e);
            throw new RuntimeException("Unexpected error occurred while executing payment", e);
        }
    }

}
