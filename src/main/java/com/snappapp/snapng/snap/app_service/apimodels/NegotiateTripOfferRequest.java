package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NegotiateTripOfferRequest {
    @Min(value = 0, message = "Amount is not valid")
    private Double amount;
    private String reference;
}
