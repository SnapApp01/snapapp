package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private Integer userCount;
    private Integer partnerCount;
    private Integer vehicleCount;
    private Integer requestCount;
}
