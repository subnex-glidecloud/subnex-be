package com.subnex.subscription.scheduler;

import com.subnex.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;

    // Every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void processRenewals() {
        LocalDate today = LocalDate.now();

        var dueSubscriptions =
                subscriptionRepository.findByNextBillingDate(today);

        dueSubscriptions.forEach(sub ->
                System.out.println("Renewal due for subscription: " + sub.getId()));
    }
}
