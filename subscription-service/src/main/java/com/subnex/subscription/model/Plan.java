package com.subnex.subscription.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    private String id;

    private String name;          // BASIC, PRO
    private Double price;
    private String currency;
    private String billingCycle;  // MONTHLY, YEARLY
    private Integer trialDays;
    private List<String> features;
    private boolean active;
}
