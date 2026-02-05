package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.CountPerDayResponse;
import com.snappapp.snapng.snap.admin.apimodels.DashboardSummaryResponse;
import com.snappapp.snapng.snap.admin.services.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/v1/admin/dashboard")
public class DashboardController {

    private final AdminDashboardService dashboardService;

    public DashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summaries")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<CountPerDayResponse> getRequestDailySummary() {
        return dashboardService.requestDailySummary(7);
    }
}
