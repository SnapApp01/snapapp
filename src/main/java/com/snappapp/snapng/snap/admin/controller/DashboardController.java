package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.CountPerDayResponse;
import com.snappapp.snapng.snap.admin.apimodels.DashboardSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/v1/admin/dashboard")
public class DashboardController {

    @GetMapping("/summaries")
    public DashboardSummaryResponse getDashboardSummary(){
        return DashboardSummaryResponse
                .builder()
                .partnerCount(1)
                .userCount(2)
                .requestCount(1)
                .vehicleCount(2)
                .build();
    }

    @GetMapping("/requests")
    public List<CountPerDayResponse> getRequestDailySummary(){
        return Arrays.asList(
                CountPerDayResponse
                        .builder()
                        .count(0)
                        .date(LocalDate.now())
                        .build(),
                CountPerDayResponse
                        .builder()
                        .count(1).date(LocalDate.now().minusDays(1))
                        .build(),
                CountPerDayResponse
                        .builder()
                        .count(0).date(LocalDate.now().minusDays(2))
                        .build(),
                CountPerDayResponse
                        .builder()
                        .count(0).date(LocalDate.now().minusDays(4))
                        .build()
        );
    }
}
