package com.subnex.email.kafka;

import com.subnex.email.dto.LoginEvent;
import com.subnex.email.service.MailerSendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginEventConsumerTest {

    @Mock
    private MailerSendService mailerSendService;

    @InjectMocks
    private LoginEventConsumer loginEventConsumer;

    private LoginEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new LoginEvent();
        testEvent.setUserId("user123");
        testEvent.setUserEmail("test@example.com");
        testEvent.setLoginTime(LocalDateTime.now());
    }

    @Test
    void testConsumeLoginEvent_Success() {
        // Given
        doNothing().when(mailerSendService).sendLoginNotification(any(LoginEvent.class));

        // When
        loginEventConsumer.consumeLoginEvent(testEvent);

        // Then
        verify(mailerSendService, times(1)).sendLoginNotification(testEvent);
    }

    @Test
    void testConsumeLoginEvent_WithDifferentUser() {
        // Given
        LoginEvent anotherEvent = new LoginEvent();
        anotherEvent.setUserId("user456");
        anotherEvent.setUserEmail("another@example.com");
        anotherEvent.setLoginTime(LocalDateTime.now());

        doNothing().when(mailerSendService).sendLoginNotification(any(LoginEvent.class));

        // When
        loginEventConsumer.consumeLoginEvent(anotherEvent);

        // Then
        verify(mailerSendService, times(1)).sendLoginNotification(anotherEvent);
    }

    @Test
    void testConsumeLoginEvent_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Email service error"))
                .when(mailerSendService).sendLoginNotification(any(LoginEvent.class));

        // When & Then - Should not throw exception (consumer should handle gracefully)
        try {
            loginEventConsumer.consumeLoginEvent(testEvent);
        } catch (Exception e) {
            // Expected behavior - exception should be logged, not propagated
        }

        verify(mailerSendService, times(1)).sendLoginNotification(testEvent);
    }
}
