package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.AddPlannedTripDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PlannedTripService {
    PlannedTrip getPlannedTrip(String reference);
    List<PlannedTrip> getPlannedTrips();
    List<PlannedTrip> getPlannedTrips(Business business);
    PlannedTrip save(AddPlannedTripDto dto, Business business);
    PlannedTrip update(PlannedTripStatus status, String reference, String businessId);
}
