package com.snappapp.snapng.snap.app_service.apimodels;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalRequest {
    private String narration;
    private Long amount;
}
