package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlannedTripRepository extends JpaRepository<PlannedTrip,Long> {
    Optional<PlannedTrip> findByReference(String reference);
    Page<PlannedTrip> findByBusinessAndStatus(Business business, PlannedTripStatus status, Pageable pageable);
    Page<PlannedTrip> findByStatus(PlannedTripStatus status, Pageable pageable);
    List<PlannedTrip> findByBusiness(Business business);
}
