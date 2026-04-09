package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Data;

@Data
public class TransferRecipientResponse {
    private boolean status;
    private String message;
    private RecipientData data;
    
    @Data
    public static class RecipientData {
        private String recipientCode;
        private String name;
        private String type;
        private String currency;
        private boolean active;
        private Details details;
        
        @Data
        public static class Details {
            private String accountNumber;
            private String bankCode;
            private String bankName;
        }
    }
}