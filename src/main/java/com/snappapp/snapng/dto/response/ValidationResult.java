package com.snappapp.snapng.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private boolean isValid;
    private String errorMessage;
    
    public static ValidationResult valid() {
        return ValidationResult.builder().isValid(true).build();
    }
    
    public static ValidationResult invalid(String message) {
        return ValidationResult.builder().isValid(false).errorMessage(message).build();
    }
}