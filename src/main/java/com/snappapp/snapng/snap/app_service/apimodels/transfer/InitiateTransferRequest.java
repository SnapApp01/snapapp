package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class InitiateTransferRequest {
    private String source; // "balance"
    private Long amount; // in kobo
    private String recipient;
    private String reference;
    private String reason;
}