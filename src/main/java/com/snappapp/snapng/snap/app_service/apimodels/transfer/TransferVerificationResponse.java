package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Data;

@Data
public class TransferVerificationResponse {
    private boolean status;
    private String message;
    private TransferVerificationData data;
    
    @Data
    public static class TransferVerificationData {
        private Long amount;
        private String reference;
        private String status;
        private String transferCode;
        private String currency;
        private String reason;
        private RecipientInfo recipient;
        
        @Data
        public static class RecipientInfo {
            private String recipientCode;
            private String name;
            private String type;
            private Details details;
            
            @Data
            public static class Details {
                private String accountNumber;
                private String bankCode;
                private String bankName;
            }
        }
    }
}