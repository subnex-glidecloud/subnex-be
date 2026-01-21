package com.subnex.subscription.service;

import com.subnex.subscription.dto.CreatePlanRequest;
import com.subnex.subscription.model.Plan;
import com.subnex.subscription.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    private Plan testPlan;
    private CreatePlanRequest createPlanRequest;

    @BeforeEach
    void setUp() {
        testPlan = Plan.builder()
                .id("plan123")
                .name("Premium Plan")
                .description("Premium subscription plan")
                .price(99.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .features(Arrays.asList("Feature 1", "Feature 2", "Feature 3"))
                .active(true)
                .build();

        createPlanRequest = new CreatePlanRequest();
        createPlanRequest.setName("Basic Plan");
        createPlanRequest.setDescription("Basic subscription plan");
        createPlanRequest.setPrice(49.99);
        createPlanRequest.setCurrency("USD");
        createPlanRequest.setBillingCycle("MONTHLY");
        createPlanRequest.setFeatures(Arrays.asList("Feature 1", "Feature 2"));
    }

    @Test
    void testGetAllPlans_Success() {
        // Given
        Plan plan2 = Plan.builder()
                .id("plan124")
                .name("Basic Plan")
                .price(49.99)
                .active(true)
                .build();

        when(planRepository.findAll()).thenReturn(Arrays.asList(testPlan, plan2));

        // When
        List<Plan> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(planRepository, times(1)).findAll();
    }

    @Test
    void testGetAllPlans_EmptyList() {
        // Given
        when(planRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Plan> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(planRepository, times(1)).findAll();
    }

    @Test
    void testGetPlanById_Success() {
        // Given
        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));

        // When
        Plan result = planService.getPlanById("plan123");

        // Then
        assertNotNull(result);
        assertEquals("plan123", result.getId());
        assertEquals("Premium Plan", result.getName());
        assertEquals(99.99, result.getPrice());
        verify(planRepository, times(1)).findById("plan123");
    }

    @Test
    void testGetPlanById_NotFound() {
        // Given
        when(planRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.getPlanById("nonexistent");
        });

        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testCreatePlan_Success() {
        // Given
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> {
            Plan saved = invocation.getArgument(0);
            saved.setId("plan125");
            return saved;
        });

        // When
        Plan result = planService.createPlan(createPlanRequest);

        // Then
        assertNotNull(result);
        assertEquals("Basic Plan", result.getName());
        assertEquals(49.99, result.getPrice());
        assertEquals("USD", result.getCurrency());
        assertEquals("MONTHLY", result.getBillingCycle());
        assertTrue(result.isActive());
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    void testCreatePlan_WithDifferentCurrency() {
        // Given
        createPlanRequest.setCurrency("EUR");
        createPlanRequest.setPrice(89.99);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> {
            Plan saved = invocation.getArgument(0);
            saved.setId("plan126");
            return saved;
        });

        // When
        Plan result = planService.createPlan(createPlanRequest);

        // Then
        assertNotNull(result);
        assertEquals("EUR", result.getCurrency());
        assertEquals(89.99, result.getPrice());
        verify(planRepository, times(1)).save(argThat(plan -> 
            plan.getCurrency().equals("EUR") && plan.getPrice() == 89.99
        ));
    }

    @Test
    void testUpdatePlan_Success() {
        // Given
        CreatePlanRequest updateRequest = new CreatePlanRequest();
        updateRequest.setName("Updated Premium Plan");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(119.99);
        updateRequest.setCurrency("USD");
        updateRequest.setBillingCycle("MONTHLY");
        updateRequest.setFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3", "Feature 4"));

        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Plan result = planService.updatePlan("plan123", updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Updated Premium Plan", result.getName());
        assertEquals(119.99, result.getPrice());
        assertEquals(4, result.getFeatures().size());
        verify(planRepository, times(1)).findById("plan123");
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_NotFound() {
        // Given
        when(planRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.updatePlan("nonexistent", createPlanRequest);
        });

        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository, times(1)).findById("nonexistent");
        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    void testDeletePlan_Success() {
        // Given
        when(planRepository.findById("plan123")).thenReturn(Optional.of(testPlan));
        doNothing().when(planRepository).deleteById("plan123");

        // When
        planService.deletePlan("plan123");

        // Then
        verify(planRepository, times(1)).findById("plan123");
        verify(planRepository, times(1)).deleteById("plan123");
    }

    @Test
    void testDeletePlan_NotFound() {
        // Given
        when(planRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.deletePlan("nonexistent");
        });

        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository, times(1)).findById("nonexistent");
        verify(planRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetActivePlans_Success() {
        // Given
        Plan inactivePlan = Plan.builder()
                .id("plan125")
                .name("Inactive Plan")
                .active(false)
                .build();

        when(planRepository.findAll()).thenReturn(Arrays.asList(testPlan, inactivePlan));

        // When
        List<Plan> allPlans = planService.getAllPlans();
        List<Plan> activePlans = allPlans.stream().filter(Plan::isActive).toList();

        // Then
        assertEquals(2, allPlans.size());
        assertEquals(1, activePlans.size());
        assertTrue(activePlans.get(0).isActive());
    }
}
