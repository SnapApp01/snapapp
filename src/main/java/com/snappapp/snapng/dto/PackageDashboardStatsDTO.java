package com.snappapp.snapng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDashboardStatsDTO {
    private Long totalPackages;
    private Long publishedPackages;
    private Long draftPackages;
    private Long archivedPackages;
    private Long availablePackages;
}