package com.subnex.subscription.controller;

import com.subnex.subscription.dto.SubscribeRequest;
import com.subnex.subscription.model.Subscription;
import com.subnex.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/{id}")
    public Subscription getSubscription(@PathVariable String id) {
        return subscriptionService.getSubscriptionById(id);
    }

    @PostMapping
    public Subscription subscribe(@RequestBody SubscribeRequest request) {
        return subscriptionService.subscribe(request);
    }

    @DeleteMapping("/{id}")
    public Subscription cancel(@PathVariable String id) {
        return subscriptionService.cancelSubscription(id);
    }

    @GetMapping("/user/{userId}")
    public List<Subscription> getUserSubscriptions(@PathVariable String userId) {
        return subscriptionService.getSubscriptionsByUserId(userId);
    }
}
