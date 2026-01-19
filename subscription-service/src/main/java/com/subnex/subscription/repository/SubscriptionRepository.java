package com.subnex.subscription.repository;

import com.subnex.subscription.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    List<Subscription> findByStatus(String status);

    List<Subscription> findByNextBillingDate(LocalDate date);

    List<Subscription> findByUserId(String userId);
}
