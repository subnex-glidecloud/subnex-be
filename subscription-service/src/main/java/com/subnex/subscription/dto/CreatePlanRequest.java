package com.subnex.subscription.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePlanRequest {
    private String name;
    private Double price;
    private String currency;
    private String billingCycle;
    private Integer trialDays;
    private List<String> features;
}
