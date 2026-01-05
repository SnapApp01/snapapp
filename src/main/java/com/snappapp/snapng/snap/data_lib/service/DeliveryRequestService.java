package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.RequestCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;

import java.util.List;

public interface DeliveryRequestService {
    DeliveryRequest createRequest(RequestCreationDto dto);
    DeliveryRequest updateStatus(String trackingId, DeliveryRequestStatus status);
    DeliveryRequest assignToVehicleWithProposal(DeliveryPriceProposal proposal);
    DeliveryRequest assignToTrip(PlannedTripOffer offer, DeliveryRequest request);
    DeliveryRequest get(String trackingId);
    DeliveryRequest get(String trackingId, SnapUser user);
    DeliveryRequest get(String trackingId, Business business);
    List<DeliveryRequest> getForUser(SnapUser user);
    List<DeliveryRequest> getNotConfirmedForUser(SnapUser user);
    List<DeliveryRequest> getPendingRequestsForUser(SnapUser user);
    List<DeliveryRequest> getForBusiness(Business business);
    List<DeliveryRequest> getForVehicle(Vehicle vehicle);
    Boolean vehicleActiveRequest(Vehicle vehicle);
    List<DeliveryRequest> getUnAssigned();
    List<DeliveryRequest> getUnAssignedNotInstant();
    List<DeliveryRequest> getUnAssignedNotInstant(List<VehicleType> vehicleTypes);
    List<DeliveryRequest> getUnAssignedInstant();
    List<DeliveryRequest> getUnAssignedInstant(List<VehicleType> vehicleTypes);
}
