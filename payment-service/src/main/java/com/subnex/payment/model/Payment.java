package com.subnex.payment.model;

import com.subnex.payment.enums.PaymentStatus;
import com.subnex.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    private String id;
    
    private String subscriptionId;
    private String userId;
    private String userEmail; // Email for notifications
    private String stripePaymentIntentId;
    private String clientSecret;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private PaymentType type;
    private Integer attempt;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

