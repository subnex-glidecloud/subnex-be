package com.subnex.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    private String eventType; // PAYMENT_SUCCESS or PAYMENT_FAILED
    private String subscriptionId;
    private String userId;
    private String userEmail; // NEW: Email to send notification to
    private Long amount;
    private String currency;
    private String reason; // for failures
    private LocalDateTime timestamp;
}
