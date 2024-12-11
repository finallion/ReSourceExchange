package com.resexchange.app.services;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaypalService {


    @Autowired
    private APIContext apiContext;

    /**
     * Create a PayPal payment
     */
    public Payment createPayment(String intent, String payerId, String currency,
                                 String total, String description,
                                 String cancelUrl, String successUrl) throws PayPalRESTException {
        Payment payment = new Payment();

        payment.setIntent(intent);
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        payment.setPayer(payer);

        payment.setRedirectUrls(new com.paypal.api.payments.RedirectUrls()
                .setCancelUrl(cancelUrl)
                .setReturnUrl(successUrl));

        String uppercaseCurrency = currency.toUpperCase();

        Amount amount = new Amount()
                .setCurrency(uppercaseCurrency)
                .setTotal(total);

        Transaction transaction = (Transaction) new Transaction()
                .setDescription(description)
                .setAmount(amount);

        payment.setTransactions(List.of(transaction));

        return payment.create(apiContext);
    }

    /**
     * Execute a PayPal payment
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecute);
    }
}
