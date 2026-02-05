package com.snappapp.snapng.snap.admin.services;

import com.snappapp.snapng.snap.admin.apimodels.CountPerDayResponse;
import com.snappapp.snapng.snap.admin.apimodels.DashboardSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

public interface AdminDashboardService {

    DashboardSummaryResponse getSummary();

    List<CountPerDayResponse> requestDailySummary(int i);
}
