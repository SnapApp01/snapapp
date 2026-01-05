package com.snappapp.snapng.dto.request.authDTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InitiatePasswordReset {
    @NotBlank
    @Email
    private String email;
}
