package com.subnex.subscription.repository;

import com.subnex.subscription.model.Plan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class PlanRepositoryTest {

    @Autowired
    private PlanRepository planRepository;

    private Plan testPlan1;
    private Plan testPlan2;

    @BeforeEach
    void setUp() {
        testPlan1 = Plan.builder()
                .name("Premium Plan")
                .description("Premium subscription with all features")
                .price(99.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1", "Feature 2", "Feature 3"))
                .active(true)
                .build();

        testPlan2 = Plan.builder()
                .name("Basic Plan")
                .description("Basic subscription with limited features")
                .price(49.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1", "Feature 2"))
                .active(true)
                .build();

        planRepository.save(testPlan1);
        planRepository.save(testPlan2);
    }

    @AfterEach
    void tearDown() {
        planRepository.deleteAll();
    }

    @Test
    void testFindAll_Success() {
        // When
        List<Plan> plans = planRepository.findAll();

        // Then
        assertNotNull(plans);
        assertEquals(2, plans.size());
    }

    @Test
    void testFindById_Success() {
        // Given
        String planId = testPlan1.getId();

        // When
        Optional<Plan> plan = planRepository.findById(planId);

        // Then
        assertTrue(plan.isPresent());
        assertEquals("Premium Plan", plan.get().getName());
        assertEquals(99.99, plan.get().getPrice());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<Plan> plan = planRepository.findById("nonexistent");

        // Then
        assertFalse(plan.isPresent());
    }

    @Test
    void testSavePlan_Success() {
        // Given
        Plan newPlan = Plan.builder()
                .name("Enterprise Plan")
                .description("Enterprise subscription for large teams")
                .price(299.99)
                .currency("USD")
                .billingCycle("YEARLY")
                .features(Arrays.asList("All Features", "Priority Support", "Custom Integration"))
                .active(true)
                .build();

        // When
        Plan savedPlan = planRepository.save(newPlan);

        // Then
        assertNotNull(savedPlan.getId());
        assertEquals("Enterprise Plan", savedPlan.getName());
        assertEquals(299.99, savedPlan.getPrice());
        assertEquals("YEARLY", savedPlan.getBillingCycle());
    }

    @Test
    void testUpdatePlan_Success() {
        // Given
        Plan plan = planRepository.findById(testPlan1.getId()).orElseThrow();
        
        // When
        plan.setPrice(119.99);
        plan.setDescription("Updated Premium Plan");
        Plan updatedPlan = planRepository.save(plan);

        // Then
        assertEquals(119.99, updatedPlan.getPrice());
        assertEquals("Updated Premium Plan", updatedPlan.getDescription());

        // Verify persistence
        Plan retrievedPlan = planRepository.findById(updatedPlan.getId()).orElseThrow();
        assertEquals(119.99, retrievedPlan.getPrice());
    }

    @Test
    void testDeletePlan_Success() {
        // Given
        String planId = testPlan2.getId();

        // When
        planRepository.deleteById(planId);
        Optional<Plan> deletedPlan = planRepository.findById(planId);

        // Then
        assertFalse(deletedPlan.isPresent());
    }

    @Test
    void testFindByActive_Success() {
        // Given
        Plan inactivePlan = Plan.builder()
                .name("Inactive Plan")
                .description("This plan is inactive")
                .price(79.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1"))
                .active(false)
                .build();
        planRepository.save(inactivePlan);

        // When
        List<Plan> allPlans = planRepository.findAll();
        long activeCount = allPlans.stream().filter(Plan::isActive).count();
        long inactiveCount = allPlans.stream().filter(p -> !p.isActive()).count();

        // Then
        assertEquals(3, allPlans.size());
        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    void testFindByCurrency() {
        // Given
        Plan euroPlan = Plan.builder()
                .name("Euro Plan")
                .description("Plan in EUR")
                .price(89.99)
                .currency("EUR")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1"))
                .active(true)
                .build();
        planRepository.save(euroPlan);

        // When
        List<Plan> allPlans = planRepository.findAll();
        long usdCount = allPlans.stream().filter(p -> p.getCurrency().equals("USD")).count();
        long eurCount = allPlans.stream().filter(p -> p.getCurrency().equals("EUR")).count();

        // Then
        assertEquals(2, usdCount);
        assertEquals(1, eurCount);
    }

    @Test
    void testUpdateFeatures_Success() {
        // Given
        Plan plan = planRepository.findById(testPlan1.getId()).orElseThrow();
        List<String> newFeatures = Arrays.asList("Feature 1", "Feature 2", "Feature 3", "Feature 4", "Feature 5");

        // When
        plan.setFeatures(newFeatures);
        Plan updatedPlan = planRepository.save(plan);

        // Then
        assertEquals(5, updatedPlan.getFeatures().size());
        assertTrue(updatedPlan.getFeatures().contains("Feature 5"));
    }
}
