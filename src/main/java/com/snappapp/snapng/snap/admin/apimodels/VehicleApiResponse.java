package com.snappapp.snapng.snap.admin.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VehicleApiResponse {
    private String id;
    private String plateNumber;
    private String owner;
    private String type;
    private boolean enabled;
}
