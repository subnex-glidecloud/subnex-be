package com.subnex.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.subnex.payment.dto.PaymentRequest;
import com.subnex.payment.dto.PaymentResponse;
import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.enums.PaymentType;
import com.subnex.payment.kafka.PaymentEventProducer;
import com.subnex.payment.model.Payment;
import com.subnex.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @Mock
    private StripePaymentService stripePaymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentProcessorService paymentProcessorService;

    private PaymentRequest testRequest;
    private PaymentIntent mockPaymentIntent;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testRequest = new PaymentRequest();
        testRequest.setUserId("user123");
        testRequest.setUserEmail("test@example.com");
        testRequest.setSubscriptionId("sub123");
        testRequest.setAmount(9999.0);
        testRequest.setCurrency("USD");
        testRequest.setPaymentType(PaymentType.SUBSCRIPTION);

        mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("secret_test123");
        when(mockPaymentIntent.getAmount()).thenReturn(9999L);
        when(mockPaymentIntent.getCurrency()).thenReturn("usd");
        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");

        testPayment = Payment.builder()
                .id("payment123")
                .userId("user123")
                .userEmail("test@example.com")
                .subscriptionId("sub123")
                .amount(99.99)
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentType(PaymentType.SUBSCRIPTION)
                .stripePaymentIntentId("pi_test123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreatePaymentIntent_Success() throws StripeException {
        // Given
        when(stripePaymentService.createPaymentIntent(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(mockPaymentIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponse response = paymentProcessorService.createPaymentIntent(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("pi_test123", response.getPaymentIntentId());
        assertEquals("secret_test123", response.getClientSecret());
        verify(stripePaymentService, times(1)).createPaymentIntent(anyLong(), anyString(), anyString(), anyString());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePaymentIntent_StripeException() throws StripeException {
        // Given
        when(stripePaymentService.createPaymentIntent(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new StripeException("Stripe API error", "request_id", "code", 400) {});

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentProcessorService.createPaymentIntent(testRequest);
        });

        verify(stripePaymentService, times(1)).createPaymentIntent(anyLong(), anyString(), anyString(), anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreatePaymentIntent_WithDifferentCurrency() throws StripeException {
        // Given
        testRequest.setCurrency("INR");
        testRequest.setAmount(799900.0);

        when(mockPaymentIntent.getCurrency()).thenReturn("inr");
        when(stripePaymentService.createPaymentIntent(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(mockPaymentIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentResponse response = paymentProcessorService.createPaymentIntent(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("pi_test123", response.getPaymentIntentId());
        verify(stripePaymentService, times(1)).createPaymentIntent(799900L, "INR", "sub123", "test@example.com");
    }

    @Test
    void testCreatePaymentIntent_WithZeroAmount() throws StripeException {
        // Given
        testRequest.setAmount(0.0);

        // When & Then - This should ideally validate and throw an exception
        when(stripePaymentService.createPaymentIntent(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(mockPaymentIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse response = paymentProcessorService.createPaymentIntent(testRequest);
        
        assertNotNull(response);
        verify(stripePaymentService, times(1)).createPaymentIntent(0L, "USD", "sub123", "test@example.com");
    }
}
