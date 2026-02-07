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

    public TripPlanManagementService(
            SnapUserService userService,
            BusinessService businessService,
            PlannedTripService plannedTripService,
            LocationService locationService,
            VehicleService vehicleService,
            WalletManagementService walletManagementService,
            PushNotificationService notificationService,
            DeliveryRequestPendingPaymentService pendingPaymentService,
            PlannedTripOfferService tripOfferService,
            DeliveryRequestService requestService
    ) {
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
        log.info("create() | userId={}", userId);

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

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if (business == null) {
            log.error("No business for userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());

        if (!vehicle.getBusinessId().equals(business.getId())) {
            log.error("Vehicle ownership mismatch | vehicleId={}, businessId={}",
                    vehicle.getId(), business.getId());
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
        log.debug("Users fetched | count={}", users.size());

        users.forEach(e -> notificationService.send(
                AddAppNotificationDto.builder()
                        .message("There is a new available rider offer. Check it out")
                        .title(NotificationTitle.DELIVERY)
                        .uid(e.getIdentifier())
                        .task(NotificationTask.PLANNED_TRIP.name())
                        .taskId(reference)
                        .build()
        ));
    }

    public List<PlannedTripResponse> getTripsByBusiness(Long userId){
        log.info("getTripsByBusiness() | userId={}", userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business == null){
            log.error("No business found | userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        return plannedTripService.getPlannedTrips(business)
                .stream()
                .map(PlannedTripResponse::new)
                .toList();
    }

    public List<PlannedTripResponse> getAvailableTrips(Long userId){
        log.info("getAvailableTrips() | userId={}", userId);
        userService.getUserById(userId);
        return plannedTripService.getPlannedTrips()
                .stream()
                .map(PlannedTripResponse::new)
                .toList();
    }

    public PlannedTripResponse getTrip(String reference, Long userId){
        log.info("getTrip() | reference={}, userId={}", reference, userId);
        userService.getUserById(userId);
        return new PlannedTripResponse(plannedTripService.getPlannedTrip(reference));
    }

    public PlannedTripResponse closeTrip(String reference, Long userId){
        log.info("closeTrip() | reference={}, userId={}", reference, userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business == null){
            log.error("No business found | userId={}", userId);
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
        PlannedTripOffer offer = tripOfferService.accept(reference, userId);

        DeliveryRequest deliveryRequest =
                createDeliveryRequestFromTripOffer(user, offer);

        return DeliveryRequestCreationResponse.builder()
                .status(deliveryRequest.getStatus())
                .trackId(deliveryRequest.getTrackingId())
                .build();
    }

    public List<TripOfferResponse> getAllTripOffers(String tripRef, Long userId) {
        log.info("getAllTripOffers() | tripRef={}, userId={}", tripRef, userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        PlannedTrip trip = plannedTripService.getPlannedTrip(tripRef);

        if(business == null ||
                !business.getCode().equalsIgnoreCase(trip.getBusiness().getCode())){
            log.error("Unauthorized access to trip offers");
            throw new ResourceNotFoundException("Trip is not available to this rider");
        }

        return tripOfferService.get(trip)
                .stream()
                .map(TripOfferResponse::new)
                .toList();
    }

    public List<TripOfferResponse> getAcceptedTripOffers(String tripRef, Long userId) {
        log.info("getAcceptedTripOffers() | tripRef={}, userId={}", tripRef, userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        PlannedTrip trip = plannedTripService.getPlannedTrip(tripRef);

        if(business == null ||
                !business.getCode().equalsIgnoreCase(trip.getBusiness().getCode())){
            log.error("Unauthorized access to accepted trip offers");
            throw new ResourceNotFoundException("Trip is not available to this rider");
        }

        return tripOfferService.getAccepted(trip)
                .stream()
                .map(TripOfferResponse::new)
                .toList();
    }

    public TripOfferResponse getOffer(String reference, Long userId) {
        log.info("getOffer() | reference={}, userId={}", reference, userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business != null){
            PlannedTripOffer tripOffer = tripOfferService.get(reference);
            if(business.getCode()
                    .equalsIgnoreCase(tripOffer.getTrip().getBusiness().getCode())){
                return new TripOfferResponse(tripOffer);
            }
        }

        log.error("Trip offer not available");
        throw new ResourceNotFoundException("Trip is not available to this user");
    }

    public TripOfferResponse getTripOffer(String tripRef, Long userId) {
        log.info("getTripOffer() | tripRef={}, userId={}", tripRef, userId);

        SnapUser user = userService.getUserById(userId);
        PlannedTrip trip = plannedTripService.getPlannedTrip(tripRef);

        return new TripOfferResponse(tripOfferService.get(user, trip));
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

        log.error("Trip not found for user");
        throw new ResourceNotFoundException("Trip not found for this user");
    }

    public TripOfferResponse rejectOffer(String reference, String userId){
        log.info("rejectOffer() | reference={}, userId={}", reference, userId);
        return new TripOfferResponse(tripOfferService.reject(reference));
    }

    public TripOfferResponse createTripOffer(CreateTripOfferRequest request, Long userId){
        log.info("createTripOffer() | tripRef={}, userId={}",
                request.getTripReference(), userId);

        SnapUser user = userService.getUserById(userId);
        PlannedTrip trip = plannedTripService.getPlannedTrip(
                request.getTripReference());

        Location pickup = locationService.addLocation(
                LocationCreationDto.builder()
                        .address(request.getPickup().getAddress())
                        .city(request.getPickup().getCity().trim())
                        .state(request.getPickup().getState())
                        .landmark(request.getPickup().getLandMark())
                        .latitude(request.getPickup().getLatitude())
                        .longitude(request.getPickup().getLongitude())
                        .build()
        );

        Location destination = locationService.addLocation(
                LocationCreationDto.builder()
                        .address(request.getDestination().getAddress().trim())
                        .city(request.getDestination().getCity().trim())
                        .state(request.getDestination().getState())
                        .landmark(request.getDestination().getLandMark())
                        .latitude(request.getDestination().getLatitude())
                        .longitude(request.getDestination().getLongitude())
                        .build()
        );

        PlannedTripOffer tripOffer = tripOfferService.save(
                new AddTripOfferDto(
                        request.getDescription(),
                        MoneyUtilities.fromDoubleToMinor(request.getWorth()),
                        request.getWeight(),
                        pickup,
                        destination,
                        request.getName(),
                        request.getPhone(),
                        MoneyUtilities.fromDoubleToMinor(request.getOffer()),
                        request.getNote()
                ),
                trip,
                user
        );

        notificationService.send(
                AddAppNotificationDto.builder()
                        .title(NotificationTitle.DELIVERY)
                        .message("There is a new delivery request for your planned trip")
                        .uid(businessService
                                .getUserOfBusiness(trip.getBusiness())
                                .getIdentifier())
                        .taskId(tripOffer.getTrip().getReference())
                        .task(NotificationTask.PLANNED_TRIP_RIDER.name())
                        .build()
        );

        return new TripOfferResponse(tripOffer);
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
                                .vehicleType(
                                        offer.getTrip().getVehicle().getType())
                                .worth(offer.getWorth())
                                .startTime(
                                        offer.getTrip().getTripDate()
                                                .atTime(LocalTime.now()))
                                .endTime(
                                        offer.getTrip().getTripDate()
                                                .atTime(LocalTime.now()))
                                .calculatedFee(offer.getUserProposedFee())
                                .build()
                );

        deliveryRequest = requestService.assignToTrip(offer, deliveryRequest);

        try {
            pendingPaymentService.create(deliveryRequest);
            walletManagementService.startRequestPayment(deliveryRequest);
            pendingPaymentService.markPaid(deliveryRequest);
            log.info("Payment completed");
        } catch (Exception e) {
            log.error("Payment failed", e);
        }

        notificationService.send(
                AddAppNotificationDto.builder()
                        .message("Your trip offer has just been accepted")
                        .title(NotificationTitle.DELIVERY)
                        .uid(deliveryRequest.getBusinessUserId())
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .taskId(deliveryRequest.getTrackingId())
                        .build()
        );

        notificationService.send(
                AddAppNotificationDto.builder()
                        .message("An order has been created for your trip offer")
                        .title(NotificationTitle.DELIVERY)
                        .uid(deliveryRequest.getUser().getIdentifier())
                        .task(NotificationTask.USER_DELIVERY.name())
                        .taskId(deliveryRequest.getTrackingId())
                        .build()
        );

        return deliveryRequest;
    }
}
