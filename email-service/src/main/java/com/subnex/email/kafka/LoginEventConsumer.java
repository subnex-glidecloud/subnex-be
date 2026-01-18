package com.subnex.email.kafka;

import com.subnex.email.dto.LoginEvent;
import com.subnex.email.service.MailerSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginEventConsumer {

    private final MailerSendService mailerSendService;

    @KafkaListener(topics = "login-events", groupId = "email-service-group")
    public void consume(LoginEvent event) {
        try {
            log.info("Received login event for user: {}", event.getUserEmail());
            mailerSendService.sendLoginNotification(event);
            log.info("Successfully sent login notification to {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to process login event: {}", e.getMessage(), e);
        }
    }
}
