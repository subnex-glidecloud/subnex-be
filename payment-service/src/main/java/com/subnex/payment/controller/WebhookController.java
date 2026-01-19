package com.subnex.payment.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.subnex.payment.service.PaymentProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentProcessorService paymentProcessorService;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            log.info("📨 Received Stripe webhook - Signature Header: {}", sigHeader != null ? "✓ Present" : "✗ Missing");
            log.debug("Webhook payload: {}", payload);

            // Verify webhook signature
            if (webhookSecret == null || webhookSecret.isEmpty()) {
                log.warn("⚠️  STRIPE_WEBHOOK_SECRET not configured - skipping signature verification");
            } else {
                Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
                log.info("✓ Webhook signature verified successfully");
                processWebhookEvent(event);
                return ResponseEntity.ok("Webhook received");
            }

        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("❌ Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
        
        return ResponseEntity.ok("Webhook received");
    }

    private void processWebhookEvent(Event event) {
        log.info("🔔 Processing Stripe webhook event ID: {}, Type: {}", event.getId(), event.getType());

        try {
            String paymentIntentId = null;

            // 🎯 Handle payment_intent.* events
            if (event.getType().startsWith("payment_intent.")) {
                paymentIntentId = extractPaymentIntentId(event);
                
                if (paymentIntentId == null) {
                    log.warn("⚠️ Could not extract PaymentIntent ID from {}", event.getType());
                    return;
                }

                if ("payment_intent.succeeded".equals(event.getType())) {
                    log.info("✅ Payment succeeded event - PaymentIntent ID: {}", paymentIntentId);
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "succeeded");
                } 
                else if ("payment_intent.payment_failed".equals(event.getType())) {
                    log.info("❌ Payment failed event - PaymentIntent ID: {}", paymentIntentId);
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "failed");
                }
                else if ("payment_intent.processing".equals(event.getType())) {
                    log.info("⏳ Payment processing event - PaymentIntent ID: {}", paymentIntentId);
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "processing");
                }
                else if ("payment_intent.requires_action".equals(event.getType())) {
                    log.info("🔐 Payment requires action event - PaymentIntent ID: {}", paymentIntentId);
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "requires_action");
                }
                else {
                    log.debug("ℹ️ Received payment_intent event: {}", event.getType());
                }
            }
            // 🎯 Handle charge.* events (nested PaymentIntent)
            else if (event.getType().startsWith("charge.")) {
                paymentIntentId = extractPaymentIntentId(event);
                
                if (paymentIntentId == null) {
                    log.debug("ℹ️ Charge event with no PaymentIntent: {}", event.getType());
                    return;
                }

                if ("charge.succeeded".equals(event.getType())) {
                    log.info("✅ Charge succeeded for PaymentIntent: {}", paymentIntentId);
                    // Update payment status based on charge success
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "succeeded");
                }
                else if ("charge.failed".equals(event.getType())) {
                    log.info("❌ Charge failed for PaymentIntent: {}", paymentIntentId);
                    paymentProcessorService.processPaymentWebhook(paymentIntentId, "failed");
                }
                else {
                    log.debug("ℹ️ Ignoring charge event: {}", event.getType());
                }
            }
            else {
                log.debug("ℹ️ Ignoring event type: {}", event.getType());
            }

        } catch (Exception e) {
            log.error("❌ Error processing webhook event: {}", e.getMessage(), e);
        }
    }

    private String extractPaymentIntentId(Event event) {
        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            // 1️⃣ Best case: Stripe could deserialize strongly
            if (deserializer.getObject().isPresent()) {
                Object obj = deserializer.getObject().get();
                if (obj instanceof PaymentIntent) {
                    return ((PaymentIntent) obj).getId();
                }
            }

            // 2️⃣ Fallback: Parse raw JSON string
            String rawJson = deserializer.getRawJson();
            if (rawJson != null && !rawJson.isEmpty()) {
                JsonObject json = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();

                // payment_intent.* events
                if (json.has("id") && json.get("id").getAsString().startsWith("pi_")) {
                    log.debug("✓ Found PaymentIntent ID in root: {}", json.get("id").getAsString());
                    return json.get("id").getAsString();
                }

                // charge.* events (has nested payment_intent field)
                if (json.has("payment_intent")) {
                    String paymentIntentId = json.get("payment_intent").getAsString();
                    log.debug("✓ Found nested PaymentIntent ID: {}", paymentIntentId);
                    return paymentIntentId;
                }
            }

            log.warn("⚠️ No PaymentIntent ID found in event type: {}", event.getType());
            return null;

        } catch (Exception e) {
            log.error("❌ Failed to extract PaymentIntent ID from event: {}", e.getMessage(), e);
            return null;
        }
    }
}
