package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.DeliveryAlreadyAssignedException;
import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.RequestCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.*;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestRepository;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestService;
import com.snappapp.snapng.snap.data_lib.service.VehicleService;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DeliveryRequestServiceImpl implements DeliveryRequestService {
    private final DeliveryRequestRepository repo;
    private final VehicleService vehicleService;
    private final BusinessService businessService;

    public DeliveryRequestServiceImpl(DeliveryRequestRepository repo, VehicleService vehicleService, BusinessService businessService) {
        this.repo = repo;
        this.vehicleService = vehicleService;
        this.businessService = businessService;
    }

    @Override
    public DeliveryRequest createRequest(RequestCreationDto dto) {
        DeliveryRequest request = new DeliveryRequest();
        request.setStatus(DeliveryRequestStatus.NEW);
        request.setTrackingId(getTrackingId());
        request.setAdditionalNote(dto.getAdditionalNote().trim());
        request.setCalculatedFee(dto.getCalculatedFee());
        request.setDeliveryFrequency(dto.getDeliveryFrequency());
        request.setDescription(dto.getDescription().trim());
        request.setSendType(dto.getSendType());
        request.setDestinationLocation(dto.getDestinationLocation());
        request.setPickupLocation(dto.getPickupLocation());
        request.setEndTime(dto.getEndTime());
        request.setRecipientName(dto.getRecipientName());
        request.setRecipientNumber(dto.getRecipientNumber());
        request.setStartTime(dto.getStartTime());
        request.setUser(dto.getUser());
        request.setVehicleType(dto.getVehicleType());
        request.setWorth(dto.getWorth());
        request.setWeight(dto.getWeight());
        return repo.save(request);
    }

    @Override
    public DeliveryRequest getDeliveryRequestById(Long deliveryRequestId) {

        return repo.findById(deliveryRequestId)
                .orElseThrow(() ->
                        new FailedProcessException("Delivery request not found"));
    }

    @Override
    public DeliveryRequest updateStatus(String trackingId, DeliveryRequestStatus status) {
        DeliveryRequest request = get(trackingId);
        request.setStatus(status);
        return repo.save(request);
    }

//    @Override
//    public DeliveryRequest assignToVehicleWithProposal(DeliveryPriceProposal proposal) {
//        if(!FeeProposalStatus.ACCEPTED.equals(proposal.getStatus())){
//            return proposal.getRequest();
//        }
//        DeliveryRequest request = proposal.getRequest();
//        if(request.getVehicle()!=null || !DeliveryRequestStatus.NEW.equals(request.getStatus())){
//            throw new DeliveryAlreadyAssignedException(String.format("Delivery request with tracking id %s already exist"));
//        }
//        request.setAgreedFee(proposal.getFee());
//        request.setVehicle(proposal.getVehicle());
//        request.setBusiness(proposal.getVehicle().getBusiness());
//        request.getBusiness().getUsers();
//        request.setBusinessUserId(request.getBusiness().getUsers().iterator().next().getIdentifier());
//        request.setStatus(DeliveryRequestStatus.AWAITING_PAYMENT);
//        return repo.save(request);
//    }

    @Override
    public DeliveryRequest assignToVehicleWithProposal(DeliveryPriceProposal proposal) {

        if (!FeeProposalStatus.ACCEPTED.equals(proposal.getStatus())) {
            return getDeliveryRequestById(proposal.getDeliveryRequestId());
        }

        DeliveryRequest request =
                getDeliveryRequestById(proposal.getDeliveryRequestId());

        if (request.getVehicle() != null
                || !DeliveryRequestStatus.NEW.equals(request.getStatus())) {

            throw new DeliveryAlreadyAssignedException(
                    String.format("Delivery request with tracking id %s already exist",
                            request.getTrackingId())
            );
        }

        Vehicle vehicle =
                vehicleService.getVehicleById(proposal.getVehicleId());

        Business business =
                businessService.getBusinessById(vehicle.getBusinessId());

        request.setAgreedFee(proposal.getFee());
        request.setVehicle(vehicle);              // keep existing mapping
        request.setBusiness(business);            // keep existing mapping

        // preserve your old lazy-loading side-effect
        business.getUsers();

        request.setBusinessUserId(
                business.getUsers()
                        .iterator()
                        .next()
                        .getIdentifier()
        );

        request.setStatus(DeliveryRequestStatus.AWAITING_PAYMENT);

        return repo.save(request);
    }


    @Override
    public DeliveryRequest assignToTrip(PlannedTripOffer offer, DeliveryRequest request) {
        if(!TripOfferStatus.ACCEPTED.equals(offer.getStatus())){
            return request;
        }
        if(request.getVehicle()!=null || !DeliveryRequestStatus.NEW.equals(request.getStatus())){
            throw new DeliveryAlreadyAssignedException(String.format("Delivery request with tracking id %s already exist"));
        }
        request.setAgreedFee(offer.getBusinessProposedFee());
        request.setVehicle(offer.getTrip().getVehicle());
        request.setBusiness(offer.getTrip().getBusiness());
        request.getBusiness().getUsers();
        request.setBusinessUserId(request.getBusiness().getUsers().iterator().next().getIdentifier());
        request.setStatus(DeliveryRequestStatus.AWAITING_PAYMENT);
        return repo.save(request);
    }


    @Override
    public DeliveryRequest get(String trackingId) {
        return repo.findByTrackingIdAndActiveTrue(trackingId).orElseThrow(()-> new ResourceNotFoundException(String.format("Delivery request with tracking id %s does not exist",trackingId)));
    }

    @Override
    public DeliveryRequest get(String trackingId, SnapUser user) {
        return repo.findByTrackingIdAndUserAndActiveTrue(trackingId, user).orElseThrow(() -> new ResourceNotFoundException(String.format("Delivery request with tracking id %s does not exist for this user",trackingId)));
    }

    @Override
    public DeliveryRequest get(String trackingId, Business business) {
        return repo.findByTrackingIdAndBusinessAndActiveTrue(trackingId, business).orElseThrow(()->new ResourceNotFoundException(String.format("Delivery request with tracking id %s does not exist for this business",trackingId)));
    }

    @Override
    public List<DeliveryRequest> getForUser(SnapUser user) {
        return repo.findByUserAndActiveTrue(user);
    }

    @Override
    public List<DeliveryRequest> getNotConfirmedForUser(SnapUser user) {
        return repo.findByUserAndStatusInAndActiveTrue(user,Arrays.asList(DeliveryRequestStatus.NEW,DeliveryRequestStatus.AWAITING_PAYMENT));
    }

    @Override
    public List<DeliveryRequest> getPendingRequestsForUser(SnapUser user) {
        return repo.findByUserAndStatusInAndActiveTrue(user,Arrays.asList(DeliveryRequestStatus.NEW,
                DeliveryRequestStatus.AWAITING_PICKUP,DeliveryRequestStatus.ENROUTE,DeliveryRequestStatus.DELIVERED,
                DeliveryRequestStatus.AWAITING_PAYMENT));
    }

    @Override
    public List<DeliveryRequest> getForBusiness(Business business) {
        return repo.findByBusinessAndActiveTrueOrderByIdDesc(business);
    }

    @Override
    public List<DeliveryRequest> getForVehicle(Vehicle vehicle) {
        return repo.findByVehicleAndActiveTrue(vehicle);
    }

    public Boolean vehicleActiveRequest(Vehicle vehicle){
        return  repo.findFirstByVehicleAndStatusInAndActiveTrue(vehicle, List.of(
                DeliveryRequestStatus.AWAITING_PAYMENT,DeliveryRequestStatus.ENROUTE,
                DeliveryRequestStatus.AWAITING_PICKUP,DeliveryRequestStatus.DELIVERED)).isPresent();
    }

    @Override
    public List<DeliveryRequest> getUnAssigned() {
        return repo.findByStatusInAndActiveTrue(Collections.singletonList(DeliveryRequestStatus.NEW));
    }

    @Override
    public List<DeliveryRequest> getUnAssignedNotInstant() {
        return repo.findByStatusInAndSendTypeInAndActiveTrue(Collections.singletonList(DeliveryRequestStatus.NEW),
                Arrays.asList(SendType.RECURRING,SendType.SCHEDULED));
    }

    @Override
    public List<DeliveryRequest> getUnAssignedNotInstant(List<VehicleType> vehicleTypes) {
        return repo.findByStatusInAndSendTypeInAndVehicleTypeInAndActiveTrueOrderByIdDesc(Collections.singletonList(DeliveryRequestStatus.NEW),
                Arrays.asList(SendType.RECURRING,SendType.SCHEDULED),vehicleTypes);
    }

    @Override
    public List<DeliveryRequest> getUnAssignedInstant() {
        return repo.findByStatusInAndSendTypeInAndActiveTrue(Collections.singletonList(DeliveryRequestStatus.NEW),
                Collections.singletonList(SendType.INSTANT));
    }

    @Override
    public List<DeliveryRequest> getUnAssignedInstant(List<VehicleType> vehicleTypes) {
        return repo.findByStatusInAndSendTypeInAndVehicleTypeInAndActiveTrueOrderByIdDesc(Collections.singletonList(DeliveryRequestStatus.NEW),
                Collections.singletonList(SendType.INSTANT),vehicleTypes);
    }

    private String getTrackingId(){
        return DateTimeUtils.getCurrentLocalDateTime().format(DateTimeFormatter.ofPattern("ddMMyy")) + IdUtilities.shortUUID().toUpperCase();
    }
}
