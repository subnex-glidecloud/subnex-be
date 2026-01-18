package com.subnex.auth.service;

import com.subnex.auth.dto.LoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, LoginEvent> kafkaTemplate;
    private static final String TOPIC = "login-events";

    public void publishLoginEvent(String userId, String email) {
        try {
            LoginEvent event = LoginEvent.builder()
                    .userEmail(email)
                    .loginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .ipAddress("N/A")
                    .deviceInfo("N/A")
                    .build();

            kafkaTemplate.send(TOPIC, event);
            log.info("Published login event for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to publish login event for user {}: {}", email, e.getMessage(), e);
        }
    }
}
