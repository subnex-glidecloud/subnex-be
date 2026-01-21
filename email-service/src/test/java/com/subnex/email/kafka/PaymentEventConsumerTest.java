package com.subnex.email.kafka;

import com.subnex.email.dto.PaymentEvent;
import com.subnex.email.service.MailerSendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private MailerSendService mailerSendService;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private PaymentEvent successEvent;
    private PaymentEvent failureEvent;

    @BeforeEach
    void setUp() {
        successEvent = new PaymentEvent();
        successEvent.setUserId("user123");
        successEvent.setUserEmail("test@example.com");
        successEvent.setPaymentId("pay123");
        successEvent.setAmount(99.99);
        successEvent.setCurrency("USD");
        successEvent.setStatus("SUCCESS");

        failureEvent = new PaymentEvent();
        failureEvent.setUserId("user123");
        failureEvent.setUserEmail("test@example.com");
        failureEvent.setPaymentId("pay124");
        failureEvent.setAmount(99.99);
        failureEvent.setCurrency("USD");
        failureEvent.setStatus("FAILED");
    }

    @Test
    void testConsumePaymentEvent_Success() {
        // Given
        doNothing().when(mailerSendService).sendPaymentSuccessEmail(any(PaymentEvent.class));

        // When
        paymentEventConsumer.consumePaymentEvent(successEvent);

        // Then
        verify(mailerSendService, times(1)).sendPaymentSuccessEmail(successEvent);
        verify(mailerSendService, never()).sendPaymentFailureEmail(any(PaymentEvent.class));
    }

    @Test
    void testConsumePaymentEvent_Failure() {
        // Given
        doNothing().when(mailerSendService).sendPaymentFailureEmail(any(PaymentEvent.class));

        // When
        paymentEventConsumer.consumePaymentEvent(failureEvent);

        // Then
        verify(mailerSendService, times(1)).sendPaymentFailureEmail(failureEvent);
        verify(mailerSendService, never()).sendPaymentSuccessEmail(any(PaymentEvent.class));
    }

    @Test
    void testConsumePaymentEvent_UnknownStatus() {
        // Given
        PaymentEvent unknownEvent = new PaymentEvent();
        unknownEvent.setUserId("user123");
        unknownEvent.setUserEmail("test@example.com");
        unknownEvent.setPaymentId("pay125");
        unknownEvent.setAmount(99.99);
        unknownEvent.setCurrency("USD");
        unknownEvent.setStatus("PENDING");

        // When
        paymentEventConsumer.consumePaymentEvent(unknownEvent);

        // Then - No email should be sent for unknown status
        verify(mailerSendService, never()).sendPaymentSuccessEmail(any(PaymentEvent.class));
        verify(mailerSendService, never()).sendPaymentFailureEmail(any(PaymentEvent.class));
    }

    @Test
    void testConsumePaymentEvent_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Email service error"))
                .when(mailerSendService).sendPaymentSuccessEmail(any(PaymentEvent.class));

        // When & Then
        try {
            paymentEventConsumer.consumePaymentEvent(successEvent);
        } catch (Exception e) {
            // Expected behavior - exception should be logged
        }

        verify(mailerSendService, times(1)).sendPaymentSuccessEmail(successEvent);
    }
}
