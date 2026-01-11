package com.subnex.subscription.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    private String id;

    private String userId;
    private String planId;
    private String status; // ACTIVE, TRIAL, PAST_DUE
    private LocalDate startDate;
    private LocalDate nextBillingDate;
    private boolean autoRenew;
}
