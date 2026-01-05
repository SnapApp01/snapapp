package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VehicleCreationDto {
    private String plateNumber;
    private String description;
    private Integer year;
    private VehicleType type;
    private Business business;
}
