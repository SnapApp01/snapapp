package com.snappapp.snapng.snap.admin.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleApiResponse {
    private String id;
    private String plateNumber;
    private String owner;
    private String type;
    private boolean enabled;
}
