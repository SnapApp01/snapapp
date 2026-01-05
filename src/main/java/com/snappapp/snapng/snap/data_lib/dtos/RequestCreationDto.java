package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.entities.Location;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryFrequency;
import com.snappapp.snapng.snap.data_lib.enums.SendType;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RequestCreationDto {
    private SendType sendType;
    private VehicleType vehicleType;
    private DeliveryFrequency deliveryFrequency;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Long worth;
    private Location pickupLocation;
    private Location destinationLocation;
    private String recipientName;
    private String recipientNumber;
    private String additionalNote;
    private SnapUser user;
    private Long calculatedFee;
    private String weight;
}
