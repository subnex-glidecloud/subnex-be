package com.subnex.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

    @InjectMocks
    private StripePaymentService stripePaymentService;

    private PaymentIntent mockPaymentIntent;

    @BeforeEach
    void setUp() {
        mockPaymentIntent = mock(PaymentIntent.class);
    }

    @Test
    void testIsPaymentSuccessful_WithSucceededStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");

        // When
        boolean result = stripePaymentService.isPaymentSuccessful(mockPaymentIntent);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPaymentSuccessful_WithFailedStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("failed");

        // When
        boolean result = stripePaymentService.isPaymentSuccessful(mockPaymentIntent);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsPaymentFailed_WithRequiresPaymentMethodStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");

        // When
        boolean result = stripePaymentService.isPaymentFailed(mockPaymentIntent);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPaymentFailed_WithRequiresActionStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("requires_action");

        // When
        boolean result = stripePaymentService.isPaymentFailed(mockPaymentIntent);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPaymentFailed_WithSucceededStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");

        // When
        boolean result = stripePaymentService.isPaymentFailed(mockPaymentIntent);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsPaymentSuccessful_WithNullStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn(null);

        // When
        boolean result = stripePaymentService.isPaymentSuccessful(mockPaymentIntent);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsPaymentFailed_WithPendingStatus() {
        // Given
        when(mockPaymentIntent.getStatus()).thenReturn("pending");

        // When
        boolean result = stripePaymentService.isPaymentFailed(mockPaymentIntent);

        // Then
        assertFalse(result);
    }
}
