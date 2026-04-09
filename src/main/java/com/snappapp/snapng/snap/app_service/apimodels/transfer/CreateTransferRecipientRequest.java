package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTransferRecipientRequest {
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 10, message = "Account number must be 10 digits")
    private String accountNumber;
    
    @NotBlank(message = "Bank code is required")
    private String bankCode;
    
    @NotBlank(message = "Bank name is required")
    private String bankName;
    
    private String type = "nuban"; // nuban for Nigerian banks
    private String currency = "NGN";
}