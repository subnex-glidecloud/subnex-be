package com.subnex.subscription.service;

import com.subnex.subscription.dto.CreatePlanRequest;
import com.subnex.subscription.model.Plan;
import com.subnex.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public Plan createPlan(CreatePlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .trialDays(request.getTrialDays())
                .features(request.getFeatures())
                .active(true)
                .build();

        return planRepository.save(plan);
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Plan getPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    public Plan deactivatePlan(String id) {
        Plan plan = getPlanById(id);
        plan.setActive(false);
        return planRepository.save(plan);
    }
}
