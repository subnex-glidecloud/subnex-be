package com.subnex.payment.kafka;

import com.subnex.payment.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private static final String TOPIC = "payment-events";

    public void publishPaymentEvent(PaymentEvent event) {
        try {
            log.info("Publishing payment event: {} for subscription: {}", event.getEventType(), event.getSubscriptionId());
            kafkaTemplate.send(TOPIC, event);
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}", e.getMessage(), e);
        }
    }
}
