package com.snappapp.snapng.snap.app_service.apimodels;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalRequest {
    @NotNull
    private Long amount; // in kobo

    private String narration;

    @NotNull
    @Size(min = 10, max = 10)
    private String bankAccountNumber;

    @NotNull
    private String bankCode;

    @NotNull
    private String accountName;

    private String bankName;
}
