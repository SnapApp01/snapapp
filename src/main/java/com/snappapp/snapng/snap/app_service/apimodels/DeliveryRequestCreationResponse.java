package com.snappapp.snapng.snap.app_service.apimodels;

import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequestCreationResponse {
    private String trackId;
    private DeliveryRequestStatus status;
}
