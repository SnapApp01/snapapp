package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryFrequency;
import com.snappapp.snapng.snap.data_lib.enums.SendType;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
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
public class CreateDeliveryRequest {
    private VehicleType vehicle;
    private DeliveryFrequency frequency;
    private SendType sendType;
    private Double worth;
    @NotNull(message = "Pickup address must be provided")
    private CreateAddressRequest pickup;
    @NotNull(message = "Destination address must be provided")
    private CreateAddressRequest destination;
    private String description;
    private String note;
    private String recipient;
    private String recipientNumber;
    @DateTimeValidate(type = DateTimeType.DATETIME, message = "Incorrect date format, expected "+ DateTimeUtils.DATE_FORMAT+" "+ DateTimeUtils.TIME_FORMAT)
    private String start;
    @DateTimeValidate(type = DateTimeType.DATETIME,nullable = true)
    private String end;
    private String weight;
}
