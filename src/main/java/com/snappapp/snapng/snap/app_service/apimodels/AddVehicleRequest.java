package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddVehicleRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]{3,12}$", message = "Kindly provide a valid plate number")
    private String plateNumber;
    @Pattern(regexp = "^[a-zA-Z0-9 ]{1,100}$", message = "Invalid description passed")
    private String description;
    @Range(min = 1960, max = 2100)
    private Integer year;
    private VehicleType vehicle;
}
