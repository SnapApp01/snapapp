package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private Long userCount;
    private Long partnerCount;
    private Long vehicleCount;
    private Long requestCount;
}
