package com.subnex.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.api-key:${stripe.secret-key:}}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        if (stripeApiKey == null || stripeApiKey.isEmpty() || stripeApiKey.contains("your_")) {
            log.warn("⚠️  Stripe API key not configured - set STRIPE_API_KEY environment variable");
            log.warn("    stripeApiKey value: {}", stripeApiKey == null ? "null" : (stripeApiKey.length() > 10 ? stripeApiKey.substring(0, 10) + "..." : stripeApiKey));
            return;
        }
        Stripe.apiKey = stripeApiKey;
        log.info("✓ Stripe API initialized successfully (key length: {})", stripeApiKey.length());
        
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("⚠️  Stripe webhook secret not configured - webhooks won't be verified");
        } else {
            log.info("✓ Stripe webhook secret configured (length: {})", webhookSecret.length());
        }
    }
}

