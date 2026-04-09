package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitiateTransferToRecipientRequest {
    @NotBlank(message = "Recipient code is required")
    private String recipientCode;
    
    @NotNull(message = "Amount is required")
    private Long amount; // in kobo (e.g., 50000 = ₦500)
    
    private String reason; // Optional reason for transfer
    
    private String narration;
}