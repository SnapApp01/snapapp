package com.snappapp.snapng.snap.app_service.apimodels;

import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeliveryRequestCreationResponse {
    private String trackId;
    private DeliveryRequestStatus status;
}
