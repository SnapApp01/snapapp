package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTripOffer;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.enums.TripOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlannedTripOfferRepository extends JpaRepository<PlannedTripOffer,Long> {
    Optional<PlannedTripOffer> findByReferenceAndUser(String reference, SnapUser user);
    Optional<PlannedTripOffer> findByTripAndUser(PlannedTrip trip, SnapUser user);
    Optional<PlannedTripOffer> findByReference(String reference);
    List<PlannedTripOffer> findByTrip(PlannedTrip trip);
    List<PlannedTripOffer> findByTripAndStatusIn(PlannedTrip trip, List<TripOfferStatus> statuses);

}
