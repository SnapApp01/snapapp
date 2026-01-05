package com.snappapp.snapng.snap.admin.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DashboardSummaryResponse {
    private Integer userCount;
    private Integer partnerCount;
    private Integer vehicleCount;
    private Integer requestCount;
}
