package com.snappapp.snapng.snap.app_service.apimodels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawalRequest {
    private String narration;
    private Long amount;
}
