package com.subnex.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripePaymentService {

    public PaymentIntent createPaymentIntent(Long amount, String currency, String subscriptionId, String userEmail) throws StripeException {
        log.info("Creating Stripe PaymentIntent: amount={}, currency={}, subscription={}, email={}", 
            amount, currency, subscriptionId, userEmail);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount) // Amount in smallest currency unit (cents for USD, paise for INR)
            .setCurrency(currency.toLowerCase())
            .setDescription("Payment for subscription: " + subscriptionId)
            .putMetadata("subscriptionId", subscriptionId)
            .putMetadata("userEmail", userEmail)
            .setReceiptEmail(userEmail)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        log.info("PaymentIntent created successfully: {}", paymentIntent.getId());
        return paymentIntent;
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Retrieving PaymentIntent: {}", paymentIntentId);
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {
        log.info("Confirming PaymentIntent: {}", paymentIntentId);
        
        Map<String, Object> params = new HashMap<>();
        params.put("payment_method", paymentMethodId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.confirm(params);
    }

    public boolean isPaymentSuccessful(PaymentIntent paymentIntent) {
        return "succeeded".equals(paymentIntent.getStatus());
    }

    public boolean isPaymentFailed(PaymentIntent paymentIntent) {
        return "requires_payment_method".equals(paymentIntent.getStatus()) || 
               "requires_action".equals(paymentIntent.getStatus());
    }
}
