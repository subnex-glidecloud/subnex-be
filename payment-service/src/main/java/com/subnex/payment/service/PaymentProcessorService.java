package com.subnex.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.subnex.payment.dto.PaymentEvent;
import com.subnex.payment.dto.PaymentRequest;
import com.subnex.payment.dto.PaymentResponse;
import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.kafka.PaymentEventProducer;
import com.subnex.payment.model.Payment;
import com.subnex.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final StripePaymentService stripePaymentService;

    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for subscription: {}, user: {}, amount: {}", 
            request.getSubscriptionId(), request.getUserId(), request.getAmount());

        try {
            // Create Stripe PaymentIntent
            PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                request.getAmount(),
                request.getCurrency() != null ? request.getCurrency() : "INR",
                request.getSubscriptionId(),
                request.getUserEmail()
            );

            // Create payment record with Stripe intent ID and client secret
            Payment payment = Payment.builder()
                .subscriptionId(request.getSubscriptionId())
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(PaymentStatus.INITIATED)
                .type(request.getType())
                .attempt(1)
                .stripePaymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            payment = paymentRepository.save(payment);
            log.info("Payment record created with Stripe Intent ID: {}", paymentIntent.getId());

            return mapToResponse(payment);

        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public void processPaymentWebhook(String paymentIntentId, String status) {
        log.info("🔔 Processing payment webhook: intentId={}, status={}", paymentIntentId, status);

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> {
                log.error("❌ Payment not found for Stripe Intent: {}", paymentIntentId);
                return new RuntimeException("Payment not found for intent: " + paymentIntentId);
            });

        log.info("✓ Found payment record: {}", payment.getId());

        if ("succeeded".equals(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("✅ Payment SUCCESS for subscription: {}, user: {}", payment.getSubscriptionId(), payment.getUserEmail());
            
            PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .subscriptionId(payment.getSubscriptionId())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

            paymentEventProducer.publishPaymentEvent(event);
            log.info("📤 Published PAYMENT_SUCCESS event to Kafka");
        } else if ("processing".equals(status)) {
            payment.setStatus(PaymentStatus.PROCESSING);
            log.info("⏳ Payment PROCESSING for subscription: {}", payment.getSubscriptionId());
        } else if ("requires_action".equals(status) || "requires_payment_method".equals(status)) {
            payment.setStatus(PaymentStatus.PENDING);
            log.info("🔐 Payment PENDING for subscription: {}", payment.getSubscriptionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment failed via webhook status: " + status);
            log.warn("❌ Payment FAILED for subscription: {}, reason: {}", payment.getSubscriptionId(), status);

            PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_FAILED")
                .subscriptionId(payment.getSubscriptionId())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reason("Webhook status: " + status)
                .timestamp(LocalDateTime.now())
                .build();

            paymentEventProducer.publishPaymentEvent(event);
            log.info("📤 Published PAYMENT_FAILED event to Kafka");
        }

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info("💾 Payment record updated in MongoDB");
    }

    public PaymentResponse getPaymentById(String paymentId) {
        log.info("Fetching payment: {}", paymentId);
        return paymentRepository.findById(paymentId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .subscriptionId(payment.getSubscriptionId())
            .userId(payment.getUserId())
            .userEmail(payment.getUserEmail())
            .stripePaymentIntentId(payment.getStripePaymentIntentId())
            .clientSecret(payment.getClientSecret())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .type(payment.getType())
            .attempt(payment.getAttempt())
            .failureReason(payment.getFailureReason())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}
