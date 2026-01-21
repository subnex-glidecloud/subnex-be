package com.subnex.payment.repository;

import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.enums.PaymentType;
import com.subnex.payment.model.Payment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment1;
    private Payment testPayment2;

    @BeforeEach
    void setUp() {
        testPayment1 = Payment.builder()
                .userId("user123")
                .userEmail("test@example.com")
                .subscriptionId("sub123")
                .amount(99.99)
                .currency("USD")
                .status(PaymentStatus.SUCCESS)
                .paymentType(PaymentType.SUBSCRIPTION)
                .stripePaymentIntentId("pi_test123")
                .createdAt(LocalDateTime.now())
                .build();

        testPayment2 = Payment.builder()
                .userId("user123")
                .userEmail("test@example.com")
                .subscriptionId("sub124")
                .amount(199.99)
                .currency("USD")
                .status(PaymentStatus.FAILED)
                .paymentType(PaymentType.SUBSCRIPTION)
                .stripePaymentIntentId("pi_test124")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(testPayment1);
        paymentRepository.save(testPayment2);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    void testFindByUserId_Success() {
        // When
        List<Payment> payments = paymentRepository.findByUserId("user123");

        // Then
        assertNotNull(payments);
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getUserId().equals("user123")));
    }

    @Test
    void testFindByUserId_NotFound() {
        // When
        List<Payment> payments = paymentRepository.findByUserId("nonexistent");

        // Then
        assertNotNull(payments);
        assertTrue(payments.isEmpty());
    }

    @Test
    void testFindBySubscriptionId_Success() {
        // When
        List<Payment> payments = paymentRepository.findBySubscriptionId("sub123");

        // Then
        assertNotNull(payments);
        assertEquals(1, payments.size());
        assertEquals("sub123", payments.get(0).getSubscriptionId());
        assertEquals(PaymentStatus.SUCCESS, payments.get(0).getStatus());
    }

    @Test
    void testFindByStripePaymentIntentId_Success() {
        // When
        Optional<Payment> payment = paymentRepository.findByStripePaymentIntentId("pi_test123");

        // Then
        assertTrue(payment.isPresent());
        assertEquals("pi_test123", payment.get().getStripePaymentIntentId());
        assertEquals(99.99, payment.get().getAmount());
    }

    @Test
    void testFindByStripePaymentIntentId_NotFound() {
        // When
        Optional<Payment> payment = paymentRepository.findByStripePaymentIntentId("nonexistent");

        // Then
        assertFalse(payment.isPresent());
    }

    @Test
    void testSavePayment_Success() {
        // Given
        Payment newPayment = Payment.builder()
                .userId("user456")
                .userEmail("newuser@example.com")
                .subscriptionId("sub456")
                .amount(299.99)
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .paymentType(PaymentType.SUBSCRIPTION)
                .stripePaymentIntentId("pi_test456")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        Payment savedPayment = paymentRepository.save(newPayment);

        // Then
        assertNotNull(savedPayment.getId());
        assertEquals("user456", savedPayment.getUserId());
        assertEquals("EUR", savedPayment.getCurrency());
        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Given
        Payment payment = paymentRepository.findByStripePaymentIntentId("pi_test124").orElseThrow();
        assertEquals(PaymentStatus.FAILED, payment.getStatus());

        // When
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment updatedPayment = paymentRepository.save(payment);

        // Then
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        
        // Verify persistence
        Payment retrievedPayment = paymentRepository.findById(updatedPayment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, retrievedPayment.getStatus());
    }

    @Test
    void testFindByUserIdAndStatus() {
        // When
        List<Payment> payments = paymentRepository.findByUserId("user123");
        long successCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .count();
        long failedCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();

        // Then
        assertEquals(1, successCount);
        assertEquals(1, failedCount);
    }
}
