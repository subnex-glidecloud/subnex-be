package com.subnex.subscription.service;

import com.subnex.subscription.dto.SubscribeRequest;
import com.subnex.subscription.model.Plan;
import com.subnex.subscription.model.Subscription;
import com.subnex.subscription.repository.PlanRepository;
import com.subnex.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public Subscription getSubscriptionById(String id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }

    public Subscription subscribe(SubscribeRequest request) {

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        LocalDate startDate = LocalDate.now();
        LocalDate nextBillingDate = startDate.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .userId(request.getUserId())
                .planId(plan.getId())
                .status("ACTIVE")
                .startDate(startDate)
                .nextBillingDate(nextBillingDate)
                .autoRenew(request.isAutoRenew())
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Subscription cancelSubscription(String id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus("CANCELLED");
        return subscriptionRepository.save(subscription);
    }
}
