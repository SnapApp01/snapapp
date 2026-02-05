package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.app_service.apimodels.*;
import com.snappapp.snapng.snap.data_lib.dtos.*;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.*;
import com.snappapp.snapng.snap.data_lib.service.*;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class TripPlanManagementService {

    private final SnapUserService userService;
    private final BusinessService businessService;
    private final PlannedTripService plannedTripService;
    private final LocationService locationService;
    private final VehicleService vehicleService;
    private final WalletManagementService walletManagementService;
    private final PushNotificationService notificationService;
    private final DeliveryRequestPendingPaymentService pendingPaymentService;
    private final PlannedTripOfferService tripOfferService;
    private final DeliveryRequestService requestService;

    public TripPlanManagementService(SnapUserService userService, BusinessService businessService, PlannedTripService plannedTripService, LocationService locationService, VehicleService vehicleService, WalletManagementService walletManagementService, PushNotificationService notificationService, DeliveryRequestPendingPaymentService pendingPaymentService, PlannedTripOfferService tripOfferService, DeliveryRequestService requestService) {
        this.userService = userService;
        this.businessService = businessService;
        this.plannedTripService = plannedTripService;
        this.locationService = locationService;
        this.vehicleService = vehicleService;
        this.walletManagementService = walletManagementService;
        this.notificationService = notificationService;
        this.pendingPaymentService = pendingPaymentService;
        this.tripOfferService = tripOfferService;
        this.requestService = requestService;
    }

    public PlannedTripResponse create(Long userId, CreatePlannedTripRequest request){
        log.info("create() called | userId={}", userId);

        Location start = locationService.addLocation(
                LocationCreationDto.builder()
                        .address(request.getStart().getAddress())
                        .city(request.getStart().getCity())
                        .state(request.getStart().getState())
                        .longitude(request.getStart().getLongitude())
                        .latitude(request.getStart().getLatitude())
                        .landmark(request.getStart().getLandMark())
                        .build()
        );
        log.debug("Start location created");

        Location end = locationService.addLocation(
                LocationCreationDto.builder()
                        .address(request.getEnd().getAddress())
                        .city(request.getEnd().getCity())
                        .state(request.getEnd().getState())
                        .longitude(request.getEnd().getLongitude())
                        .latitude(request.getEnd().getLatitude())
                        .landmark(request.getEnd().getLandMark())
                        .build()
        );
        log.debug("End location created");

        SnapUser user = userService.getUserById(userId);
        log.debug("User fetched");

        Business business = businessService.getBusinessOfUser(user);
        if (business == null) {
            log.error("No business for userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
        log.debug("Vehicle fetched");

        if (!vehicle.getBusinessId().equals(business.getId())) {
            log.error("Vehicle ownership mismatch");
            throw new FailedProcessException("Vehicle is not owned by this business");
        }

        PlannedTrip trip = plannedTripService.save(
                new AddPlannedTripDto(
                        start,
                        end,
                        vehicle,
                        DateTimeUtils.parseDate(request.getDate())
                ),
                business
        );

        log.info("Planned trip created | reference={}", trip.getReference());
        return new PlannedTripResponse(trip);
    }

    @Async
    public void notifyOnPlannedTrip(String reference){
        log.info("notifyOnPlannedTrip() | reference={}", reference);

        List<SnapUser> users = userService.getUsers();
        log.debug("Users fetched for notification | count={}", users.size());

        users.forEach(e -> {
            notificationService.send(
                    AddAppNotificationDto.builder()
                            .message("There is a new available rider offer. Check it out")
                            .title(NotificationTitle.DELIVERY)
                            .uid(e.getIdentifier())
                            .task(NotificationTask.PLANNED_TRIP.name())
                            .taskId(reference)
                            .build()
            );
        });
    }

    public List<PlannedTripResponse> getTripsByBusiness(Long userId){
        log.info("getTripsByBusiness() | userId={}", userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business == null){
            log.error("No business found for userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        return plannedTripService.getPlannedTrips(business)
                .stream()
                .map(PlannedTripResponse::new)
                .toList();
    }

    public PlannedTripResponse closeTrip(String reference, Long userId){
        log.info("closeTrip() | reference={}, userId={}", reference, userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business == null){
            log.error("No business found while closing trip");
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        PlannedTrip trip = plannedTripService.update(
                PlannedTripStatus.CLOSED,
                reference,
                business.getCode()
        );

        log.info("Trip closed | reference={}", reference);
        return new PlannedTripResponse(trip);
    }

    @Transactional(rollbackOn = Exception.class)
    public DeliveryRequestCreationResponse acceptTripOffer(String reference, Long userId) {
        log.info("acceptTripOffer() | reference={}, userId={}", reference, userId);

        SnapUser user = userService.getUserById(userId);
        PlannedTripOffer offer = tripOfferService.accept(reference, user);

        log.debug("Trip offer accepted");

        DeliveryRequest deliveryRequest =
                createDeliveryRequestFromTripOffer(user, offer);

        log.info("Delivery request created | trackingId={}",
                deliveryRequest.getTrackingId());

        return DeliveryRequestCreationResponse.builder()
                .status(deliveryRequest.getStatus())
                .trackId(deliveryRequest.getTrackingId())
                .build();
    }

    public TripOfferResponse makeOffer(String reference, Long amount, Long userId){
        log.info("makeOffer() | reference={}, amount={}, userId={}",
                reference, amount, userId);

        if(amount < 0){
            log.error("Invalid offer amount");
            throw new FailedProcessException("Offer amount must be valid");
        }

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        PlannedTripOffer tripOffer = tripOfferService.get(reference);

        if(business != null &&
                business.getCode().equalsIgnoreCase(
                        tripOffer.getTrip().getBusiness().getCode())){

            tripOffer = tripOfferService.setRiderOffer(
                    tripOffer.getReference(), amount);

            log.debug("Rider offer updated | status={}", tripOffer.getStatus());

            if(TripOfferStatus.ACCEPTED.equals(tripOffer.getStatus())){
                createDeliveryRequestFromTripOffer(
                        tripOffer.getUser(), tripOffer);
            } else {
                notificationService.send(
                        AddAppNotificationDto.builder()
                                .title(NotificationTitle.DELIVERY)
                                .message("There is an update to your delivery request")
                                .uid(tripOffer.getUser().getIdentifier())
                                .taskId(tripOffer.getTrip().getReference())
                                .task(NotificationTask.PLANNED_TRIP.name())
                                .build()
                );
            }
            return new TripOfferResponse(tripOffer);
        }

        log.error("Trip not found or unauthorized");
        throw new ResourceNotFoundException("Trip not found for this user");
    }

    private DeliveryRequest createDeliveryRequestFromTripOffer(
            SnapUser user, PlannedTripOffer offer){

        log.info("createDeliveryRequestFromTripOffer()");

        DeliveryRequest deliveryRequest =
                requestService.createRequest(
                        RequestCreationDto.builder()
                                .additionalNote(offer.getAdditionalNote())
                                .deliveryFrequency(DeliveryFrequency.ONCE)
                                .description(offer.getDescription())
                                .destinationLocation(offer.getDestinationLocation())
                                .pickupLocation(offer.getPickupLocation())
                                .recipientName(offer.getRecipientName())
                                .weight(offer.getWeight())
                                .recipientNumber(offer.getRecipientNumber())
                                .user(user)
                                .sendType(SendType.SCHEDULED)
                                .vehicleType(offer.getTrip().getVehicle().getType())
                                .worth(offer.getWorth())
                                .startTime(offer.getTrip().getTripDate().atTime(LocalTime.now()))
                                .endTime(offer.getTrip().getTripDate().atTime(LocalTime.now()))
                                .calculatedFee(offer.getUserProposedFee())
                                .build()
                );

        deliveryRequest = requestService.assignToTrip(offer, deliveryRequest);
        log.debug("Delivery request assigned to trip");

        try {
            pendingPaymentService.create(deliveryRequest);
            walletManagementService.startRequestPayment(deliveryRequest);
            pendingPaymentService.markPaid(deliveryRequest);
            log.info("Payment completed");
        } catch (Exception e) {
            log.error("Payment processing failed", e);
        }

        return deliveryRequest;
    }
}
