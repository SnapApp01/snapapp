package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.SendType;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest,Long> {
    Optional<DeliveryRequest> findByTrackingIdAndActiveTrue(String trackingId);
    Optional<DeliveryRequest> findByTrackingIdAndUserAndActiveTrue(String trackingId, SnapUser user);
    Optional<DeliveryRequest> findByTrackingIdAndBusinessAndActiveTrue(String trackingId, Business business);
    List<DeliveryRequest> findByVehicleAndActiveTrue(Vehicle vehicle);
    Optional<DeliveryRequest> findFirstByVehicleAndStatusInAndActiveTrue(Vehicle vehicle,List<DeliveryRequestStatus> statuses);
    List<DeliveryRequest> findByBusinessAndActiveTrueOrderByIdDesc(Business business);
    List<DeliveryRequest> findByUserAndActiveTrue(SnapUser user);
    List<DeliveryRequest> findByUserAndStatusInAndActiveTrue(SnapUser user,List<DeliveryRequestStatus> statuses);
    List<DeliveryRequest> findByStatusInAndActiveTrue(List<DeliveryRequestStatus> statuses);
    List<DeliveryRequest> findByStatusInAndSendTypeInAndActiveTrue(List<DeliveryRequestStatus> statuses, List<SendType> sendTypes);
    List<DeliveryRequest> findByStatusInAndSendTypeInAndVehicleTypeInAndActiveTrueOrderByIdDesc(List<DeliveryRequestStatus> statuses, List<SendType> sendTypes, List<VehicleType> vehicleTypes);

    Long countByCreatedAtBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
