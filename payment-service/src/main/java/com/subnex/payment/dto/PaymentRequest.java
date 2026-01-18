package com.subnex.payment.dto;

import com.subnex.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String subscriptionId;
    private String userId;
    private String userEmail; // NEW: Email to send notification to
    private Long amount;
    private String currency;
    private PaymentType type;
}
