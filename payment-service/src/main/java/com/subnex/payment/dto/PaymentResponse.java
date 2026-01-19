package com.subnex.payment.dto;

import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String subscriptionId;
    private String userId;
    private String userEmail;
    private String stripePaymentIntentId;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private PaymentType type;
    private Integer attempt;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
