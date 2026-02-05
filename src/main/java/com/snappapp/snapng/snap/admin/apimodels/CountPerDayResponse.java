package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountPerDayResponse {
    private Long count;
    private LocalDate date;
}
