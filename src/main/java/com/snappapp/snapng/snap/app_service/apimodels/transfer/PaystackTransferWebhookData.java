package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaystackTransferWebhookData {
    private Long amount;
    private String reference;
    private String transferCode;
    private String status;
    private String currency;
    private String reason;
    
    @JsonProperty("transfer_code")
    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }
    
    private Recipient recipient;
    
    @Data
    public static class Recipient {
        private String recipientCode;
        private String name;
        private Details details;
        
        @Data
        public static class Details {
            private String accountNumber;
            private String bankName;
        }
    }
}