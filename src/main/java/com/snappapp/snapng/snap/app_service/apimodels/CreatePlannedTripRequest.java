package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.snappapp.snapng.snap.utils.annotations.DateTimeValidate;
import com.snappapp.snapng.snap.utils.enums.DateTimeType;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePlannedTripRequest {
    @NotNull(message = "Start address must be provided")
    private CreateAddressRequest start;
    @NotNull(message = "Destination address must be provided")
    private CreateAddressRequest end;
    private String vehicleId;
    @DateTimeValidate(type = DateTimeType.DATE, message = "Incorrect date format, expected "+ DateTimeUtils.DATE_FORMAT)
    private String date;
}
