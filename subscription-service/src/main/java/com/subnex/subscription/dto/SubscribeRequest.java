package com.subnex.subscription.dto;

import lombok.Data;

@Data
public class SubscribeRequest {
    private String userId;
    private String planId;
    private boolean autoRenew;
}
