package com.subnex.subscription.repository;

import com.subnex.subscription.model.Subscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription testSubscription1;
    private Subscription testSubscription2;

    @BeforeEach
    void setUp() {
        testSubscription1 = Subscription.builder()
                .userId("user123")
                .planId("plan123")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .autoRenew(true)
                .build();

        testSubscription2 = Subscription.builder()
                .userId("user123")
                .planId("plan124")
                .status("CANCELLED")
                .startDate(LocalDate.now().minusMonths(3))
                .nextBillingDate(LocalDate.now().minusMonths(2))
                .autoRenew(false)
                .build();

        subscriptionRepository.save(testSubscription1);
        subscriptionRepository.save(testSubscription2);
    }

    @AfterEach
    void tearDown() {
        subscriptionRepository.deleteAll();
    }

    @Test
    void testFindByUserId_Success() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByUserId("user123");

        // Then
        assertNotNull(subscriptions);
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.stream().allMatch(s -> s.getUserId().equals("user123")));
    }

    @Test
    void testFindByUserId_NotFound() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByUserId("nonexistent");

        // Then
        assertNotNull(subscriptions);
        assertTrue(subscriptions.isEmpty());
    }

    @Test
    void testFindById_Success() {
        // Given
        String subscriptionId = testSubscription1.getId();

        // When
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);

        // Then
        assertTrue(subscription.isPresent());
        assertEquals("user123", subscription.get().getUserId());
        assertEquals("ACTIVE", subscription.get().getStatus());
    }

    @Test
    void testSaveSubscription_Success() {
        // Given
        Subscription newSubscription = Subscription.builder()
                .userId("user456")
                .planId("plan456")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .autoRenew(true)
                .build();

        // When
        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // Then
        assertNotNull(savedSubscription.getId());
        assertEquals("user456", savedSubscription.getUserId());
        assertEquals("plan456", savedSubscription.getPlanId());
        assertEquals("ACTIVE", savedSubscription.getStatus());
    }

    @Test
    void testUpdateSubscriptionStatus_Success() {
        // Given
        Subscription subscription = subscriptionRepository.findById(testSubscription1.getId()).orElseThrow();
        assertEquals("ACTIVE", subscription.getStatus());

        // When
        subscription.setStatus("CANCELLED");
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        // Then
        assertEquals("CANCELLED", updatedSubscription.getStatus());

        // Verify persistence
        Subscription retrievedSubscription = subscriptionRepository.findById(updatedSubscription.getId()).orElseThrow();
        assertEquals("CANCELLED", retrievedSubscription.getStatus());
    }

    @Test
    void testDeleteSubscription_Success() {
        // Given
        String subscriptionId = testSubscription2.getId();

        // When
        subscriptionRepository.deleteById(subscriptionId);
        Optional<Subscription> deletedSubscription = subscriptionRepository.findById(subscriptionId);

        // Then
        assertFalse(deletedSubscription.isPresent());
    }

    @Test
    void testFindByUserIdAndStatus() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByUserId("user123");
        long activeCount = subscriptions.stream()
                .filter(s -> s.getStatus().equals("ACTIVE"))
                .count();
        long cancelledCount = subscriptions.stream()
                .filter(s -> s.getStatus().equals("CANCELLED"))
                .count();

        // Then
        assertEquals(1, activeCount);
        assertEquals(1, cancelledCount);
    }

    @Test
    void testFindByAutoRenew() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByUserId("user123");
        long autoRenewCount = subscriptions.stream()
                .filter(Subscription::isAutoRenew)
                .count();

        // Then
        assertEquals(1, autoRenewCount);
    }

    @Test
    void testUpdateNextBillingDate_Success() {
        // Given
        Subscription subscription = subscriptionRepository.findById(testSubscription1.getId()).orElseThrow();
        LocalDate newBillingDate = LocalDate.now().plusMonths(2);

        // When
        subscription.setNextBillingDate(newBillingDate);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        // Then
        assertEquals(newBillingDate, updatedSubscription.getNextBillingDate());
    }
}
