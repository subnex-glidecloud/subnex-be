package com.subnex.subscription.controller;

import com.subnex.subscription.dto.CreatePlanRequest;
import com.subnex.subscription.model.Plan;
import com.subnex.subscription.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public Plan createPlan(@RequestBody CreatePlanRequest request) {
        return planService.createPlan(request);
    }

    @GetMapping
    public List<Plan> getAllPlans() {
        return planService.getAllPlans();
    }

    @GetMapping("/{id}")
    public Plan getPlan(@PathVariable String id) {
        return planService.getPlanById(id);
    }

    @DeleteMapping("/{id}")
    public Plan deactivatePlan(@PathVariable String id) {
        return planService.deactivatePlan(id);
    }
}
