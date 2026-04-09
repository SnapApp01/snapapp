package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Data;

@Data
public class InitiateTransferResponse {
    private boolean status;
    private String message;
    private TransferData data;
    
    @Data
    public static class TransferData {
        private String transferCode;
        private String reference;
        private Long amount;
        private String currency;
        private String status;
        private String recipientCode;
        private Long id;
    }
}