package com.subnex.email.service;

import com.subnex.email.dto.LoginEvent;
import com.subnex.email.dto.PaymentEvent;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MailerSendServiceTest {

    private MailerSendService mailerSendService;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.create(baseUrl);

        mailerSendService = new MailerSendService();
        ReflectionTestUtils.setField(mailerSendService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(mailerSendService, "fromEmail", "noreply@subnex.com");
        ReflectionTestUtils.setField(mailerSendService, "fromName", "Subnex Service");
        
        // Inject the WebClient using reflection
        ReflectionTestUtils.setField(mailerSendService, "webClient", webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSendLoginNotification_Success() {
        // Given
        LoginEvent event = new LoginEvent();
        event.setUserId("user123");
        event.setUserEmail("test@example.com");
        event.setLoginTime(LocalDateTime.now());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"success\"}"));

        // When & Then
        assertDoesNotThrow(() -> mailerSendService.sendLoginNotification(event));
    }

    @Test
    void testSendPaymentSuccessEmail_Success() {
        // Given
        PaymentEvent event = new PaymentEvent();
        event.setUserId("user123");
        event.setUserEmail("test@example.com");
        event.setPaymentId("pay123");
        event.setAmount(99.99);
        event.setCurrency("USD");
        event.setStatus("SUCCESS");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"success\"}"));

        // When & Then
        assertDoesNotThrow(() -> mailerSendService.sendPaymentSuccessEmail(event));
    }

    @Test
    void testSendPaymentFailureEmail_Success() {
        // Given
        PaymentEvent event = new PaymentEvent();
        event.setUserId("user123");
        event.setUserEmail("test@example.com");
        event.setPaymentId("pay123");
        event.setAmount(99.99);
        event.setCurrency("USD");
        event.setStatus("FAILED");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"success\"}"));

        // When & Then
        assertDoesNotThrow(() -> mailerSendService.sendPaymentFailureEmail(event));
    }

    @Test
    void testSendLoginNotification_WithValidEmailAddress() {
        // Given
        LoginEvent event = new LoginEvent();
        event.setUserId("user456");
        event.setUserEmail("valid.email@test.com");
        event.setLoginTime(LocalDateTime.now());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"success\"}"));

        // When & Then
        assertDoesNotThrow(() -> mailerSendService.sendLoginNotification(event));
    }

    @Test
    void testSendPaymentEmail_WithDifferentCurrencies() {
        // Given
        PaymentEvent eventUSD = new PaymentEvent();
        eventUSD.setUserId("user123");
        eventUSD.setUserEmail("test@example.com");
        eventUSD.setPaymentId("pay123");
        eventUSD.setAmount(99.99);
        eventUSD.setCurrency("USD");
        eventUSD.setStatus("SUCCESS");

        PaymentEvent eventINR = new PaymentEvent();
        eventINR.setUserId("user124");
        eventINR.setUserEmail("test2@example.com");
        eventINR.setPaymentId("pay124");
        eventINR.setAmount(7999.00);
        eventINR.setCurrency("INR");
        eventINR.setStatus("SUCCESS");

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"success\"}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"success\"}"));

        // When & Then
        assertDoesNotThrow(() -> {
            mailerSendService.sendPaymentSuccessEmail(eventUSD);
            mailerSendService.sendPaymentSuccessEmail(eventINR);
        });
    }
}
