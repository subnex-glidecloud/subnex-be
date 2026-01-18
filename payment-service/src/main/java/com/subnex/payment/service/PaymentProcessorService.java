package com.subnex.payment.service;

import com.subnex.payment.dto.PaymentEvent;
import com.subnex.payment.dto.PaymentRequest;
import com.subnex.payment.dto.PaymentResponse;
import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.kafka.PaymentEventProducer;
import com.subnex.payment.model.Payment;
import com.subnex.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Value("${payment.success-rate:0.8}")
    private double successRate;

    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for subscription: {}, user: {}, amount: {}", 
            request.getSubscriptionId(), request.getUserId(), request.getAmount());

        // Create payment record
        Payment payment = Payment.builder()
            .subscriptionId(request.getSubscriptionId())
            .userId(request.getUserId())
            .userEmail(request.getUserEmail())
            .amount(request.getAmount())
            .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
            .status(PaymentStatus.INITIATED)
            .type(request.getType())
            .attempt(1)
            .stripePaymentIntentId("pi_" + UUID.randomUUID().toString().substring(0, 12))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created with ID: {}", payment.getId());

        // Simulate payment processing
        processPayment(payment);

        return mapToResponse(payment);
    }

    public void processPayment(Payment payment) {
        log.info("Processing payment: {}", payment.getId());

        // Mock payment logic: randomized success/failure
        boolean success = Math.random() < successRate;

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment SUCCESS for: {}", payment.getSubscriptionId());
            
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
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds");
            log.warn("Payment FAILED for: {}", payment.getSubscriptionId());

            PaymentEvent event = PaymentEvent.builder()
                .eventType("PAYMENT_FAILED")
                .subscriptionId(payment.getSubscriptionId())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reason("Insufficient funds")
                .timestamp(LocalDateTime.now())
                .build();

            paymentEventProducer.publishPaymentEvent(event);
        }

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
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
