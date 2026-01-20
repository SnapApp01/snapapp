package com.snappapp.snapng.snap.app_service.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedTripResponse {
    private AddressRetrievalResponse start;
    private AddressRetrievalResponse end;
    private VehicleRetrievalResponse vehicle;
    private LocalDate date;
    private String reference;
    private PlannedTripStatus status;

    public PlannedTripResponse(PlannedTrip trip){
        this.start = new AddressRetrievalResponse(trip.getStart());
        this.end = new AddressRetrievalResponse(trip.getEnd());
        this.vehicle = new VehicleRetrievalResponse(trip.getVehicle());
        this.date = trip.getTripDate();
        this.reference = trip.getReference();
        this.status = trip.getStatus();
    }
}
