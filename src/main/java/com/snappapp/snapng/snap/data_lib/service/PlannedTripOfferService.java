package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.AddTripOfferDto;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTripOffer;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;

import java.util.List;

public interface PlannedTripOfferService {
    PlannedTripOffer save(AddTripOfferDto dto, PlannedTrip trip, SnapUser user);
    List<PlannedTripOffer> get(PlannedTrip trip);
    List<PlannedTripOffer> getAccepted(PlannedTrip trip);
    PlannedTripOffer get(SnapUser user, PlannedTrip trip);
    PlannedTripOffer setRiderOffer(String reference,Long offer);
    PlannedTripOffer reject(String reference);
    PlannedTripOffer get(String reference);
    PlannedTripOffer accept(String reference,SnapUser user);
}
