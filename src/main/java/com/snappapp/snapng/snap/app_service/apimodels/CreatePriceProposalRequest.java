package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePriceProposalRequest {
    private String trackId;
    private Long vehicleId;
    @Min(value = 1,message = "Fee is not valid")
    private Double proposedFee;
    private String comment;
}
