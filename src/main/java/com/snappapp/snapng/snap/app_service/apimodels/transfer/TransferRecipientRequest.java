package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferRecipientRequest {
    private String type;
    private String name;
    private String accountNumber;
    private String bankCode;
    private String currency;
}