package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundWalletRequest {
    private Long amount;
    @Pattern(regexp = "^[a-zA-Z0-9,.() ]{0,65}$", message = "Address contains invalid character")
    private String narration;
}
