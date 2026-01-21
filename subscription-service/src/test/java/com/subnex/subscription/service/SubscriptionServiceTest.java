package com.subnex.subscription.service;

import com.subnex.subscription.dto.SubscribeRequest;
import com.subnex.subscription.model.Plan;
import com.subnex.subscription.model.Subscription;
import com.subnex.subscription.repository.PlanRepository;
import com.subnex.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Plan testPlan;
    private Subscription testSubscription;
    private SubscribeRequest subscribeRequest;

    @BeforeEach
    void setUp() {
        testPlan = Plan.builder()
                .id("plan123")
                .name("Premium Plan")
                .description("Premium subscription plan")
                .price(99.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1", "Feature 2"))
                .active(true)
                .build();

        testSubscription = Subscription.builder()
                .id("sub123")
                .userId("user123")
                .planId("plan123")
                .status("ACTIVE")
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .autoRenew(true)
                .build();

        subscribeRequest = new SubscribeRequest();
        subscribeRequest.setUserId("user123");
        subscribeRequest.setPlanId("plan123");
        subscribeRequest.setAutoRenew(true);
    }

    @Test
    void testGetSubscriptionById_Success() {
        // Given
        when(subscriptionRepository.findById("sub123")).thenReturn(Optional.of(testSubscription));

        // When
        Subscription result = subscriptionService.getSubscriptionById("sub123");

        // Then
        assertNotNull(result);
        assertEquals("sub123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals("ACTIVE", result.getStatus());
        verify(subscriptionRepository, times(1)).findById("sub123");
    }

    @Test
    void testGetSubscriptionById_NotFound() {
        // Given
        when(subscriptionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subscriptionService.getSubscriptionById("nonexistent");
        });

        assertEquals("Subscription not found", exception.getMessage());
        verify(subscriptionRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testSubscribe_Success() {
        // Given
        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.subscribe(subscribeRequest);

        // Then
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals("plan123", result.getPlanId());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.isAutoRenew());
        verify(planRepository, times(1)).findById("plan123");
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testSubscribe_PlanNotFound() {
        // Given
        when(planRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subscribeRequest.setPlanId("nonexistent");
            subscriptionService.subscribe(subscribeRequest);
        });

        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository, times(1)).findById("nonexistent");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testSubscribe_WithAutoRenewFalse() {
        // Given
        subscribeRequest.setAutoRenew(false);
        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            saved.setId("sub124");
            return saved;
        });

        // When
        Subscription result = subscriptionService.subscribe(subscribeRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.isAutoRenew());
        verify(subscriptionRepository, times(1)).save(argThat(sub -> !sub.isAutoRenew()));
    }

    @Test
    void testCancelSubscription_Success() {
        // Given
        when(subscriptionRepository.findById("sub123")).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Subscription result = subscriptionService.cancelSubscription("sub123");

        // Then
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(subscriptionRepository, times(1)).findById("sub123");
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testCancelSubscription_NotFound() {
        // Given
        when(subscriptionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subscriptionService.cancelSubscription("nonexistent");
        });

        assertEquals("Subscription not found", exception.getMessage());
        verify(subscriptionRepository, times(1)).findById("nonexistent");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testGetSubscriptionsByUserId_Success() {
        // Given
        Subscription sub1 = Subscription.builder()
                .id("sub123")
                .userId("user123")
                .planId("plan123")
                .status("ACTIVE")
                .build();

        Subscription sub2 = Subscription.builder()
                .id("sub124")
                .userId("user123")
                .planId("plan124")
                .status("CANCELLED")
                .build();

        when(subscriptionRepository.findByUserId("user123")).thenReturn(Arrays.asList(sub1, sub2));

        // When
        List<Subscription> result = subscriptionService.getSubscriptionsByUserId("user123");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getUserId().equals("user123")));
        verify(subscriptionRepository, times(1)).findByUserId("user123");
    }

    @Test
    void testGetSubscriptionsByUserId_EmptyList() {
        // Given
        when(subscriptionRepository.findByUserId("user456")).thenReturn(Arrays.asList());

        // When
        List<Subscription> result = subscriptionService.getSubscriptionsByUserId("user456");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subscriptionRepository, times(1)).findByUserId("user456");
    }

    @Test
    void testSubscribe_BillingDateCalculation() {
        // Given
        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            saved.setId("sub125");
            return saved;
        });

        // When
        Subscription result = subscriptionService.subscribe(subscribeRequest);

        // Then
        assertNotNull(result.getStartDate());
        assertNotNull(result.getNextBillingDate());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(LocalDate.now().plusMonths(1), result.getNextBillingDate());
    }
}
