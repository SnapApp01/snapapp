package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTripOfferRequest {
    private String description;
    private Double worth;
    private String weight;
    @NotNull(message = "Pickup address must be provided")
    private CreateAddressRequest pickup;
    @NotNull(message = "Destination address must be provided")
    private CreateAddressRequest destination;
    private String name;
    private String phone;
    private Double offer;
    private String note;
    private String tripReference;
}
