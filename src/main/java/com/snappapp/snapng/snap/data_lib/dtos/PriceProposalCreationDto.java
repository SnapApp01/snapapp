package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PriceProposalCreationDto {
    private Long vehicleId;
    private Long requestId;
    private Long amount;
    private String comment;
    private boolean businessInitiated;
}
