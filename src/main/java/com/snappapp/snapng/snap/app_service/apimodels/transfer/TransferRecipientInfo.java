package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferRecipientInfo {
    private String recipientCode;
    private String accountName;
    private String accountNumber;
    private String bankName;
    private String bankCode;
    private String currency;
    private boolean active;
}