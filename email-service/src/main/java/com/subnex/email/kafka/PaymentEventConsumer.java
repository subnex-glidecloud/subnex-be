package com.subnex.email.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.subnex.email.dto.PaymentEvent;
import com.subnex.email.service.MailerSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final MailerSendService mailerSendService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "email-service-payment-group")
    public void consumePaymentEvent(@Payload String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            log.info("Received payment event: {} for user: {}", event.getEventType(), event.getUserEmail());
            
            if ("PAYMENT_SUCCESS".equals(event.getEventType())) {
                mailerSendService.sendPaymentSuccessEmail(event);
                log.info("Successfully sent payment success email to: {}", event.getUserEmail());
            } else if ("PAYMENT_FAILED".equals(event.getEventType())) {
                mailerSendService.sendPaymentFailureEmail(event);
                log.info("Successfully sent payment failure email to: {}", event.getUserEmail());
            }
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage(), e);
        }
    }
}


