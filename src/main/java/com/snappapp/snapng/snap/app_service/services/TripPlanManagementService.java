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

    public PlannedTripResponse create(Long userId, CreatePlannedTripRequest request) {
        log.info("Creating planned trip | userId={}", userId);

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
        log.debug("Start location created | id={}", start.getId());

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
        log.debug("End location created | id={}", end.getId());

        SnapUser user = userService.getUserById(userId);
        log.debug("User resolved | identifier={}", user.getIdentifier());

        Business business = businessService.getBusinessOfUser(user);
        if (business == null) {
            log.error("User has no business | userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
        log.debug("Vehicle fetched | vehicleId={}, businessId={}", vehicle.getId(), vehicle.getBusinessId());

        if (!vehicle.getBusinessId().equals(business.getId())) {
            log.error("Vehicle ownership mismatch | vehicleId={}, businessId={}", vehicle.getId(), business.getId());
            throw new FailedProcessException("Vehicle is not owned by this business");
        }

        PlannedTrip trip = plannedTripService.save(
                new AddPlannedTripDto(start, end, vehicle, DateTimeUtils.parseDate(request.getDate())),
                business
        );

        log.info("Planned trip created successfully | reference={}", trip.getReference());
        return new PlannedTripResponse(trip);
    }

    @Async
    public void notifyOnPlannedTrip(String reference) {
        log.info("Sending planned trip notifications | reference={}", reference);
        List<SnapUser> users = userService.getUsers();
        users.forEach(user -> {
            log.debug("Sending notification to user | uid={}", user.getIdentifier());
            notificationService.send(
                    AddAppNotificationDto.builder()
                            .message("There is a new available rider offer. Check it out")
                            .title(NotificationTitle.DELIVERY)
                            .uid(user.getIdentifier())
                            .task(NotificationTask.PLANNED_TRIP.name())
                            .taskId(reference)
                            .build()
            );
        });
    }

    public List<PlannedTripResponse> getTripsByBusiness(Long userId) {
        log.info("Fetching trips by business | userId={}", userId);
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if (business == null) {
            log.error("No business found for user | userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        return plannedTripService.getPlannedTrips(business)
                .stream()
                .map(PlannedTripResponse::new)
                .toList();
    }

    public List<PlannedTripResponse> getAvailableTrips(Long userId) {
        log.info("Fetching available trips | userId={}", userId);
        userService.getUserById(userId);
        return plannedTripService.getPlannedTrips()
                .stream()
                .map(PlannedTripResponse::new)
                .toList();
    }

    public PlannedTripResponse getTrip(String reference, Long userId) {
        log.info("Fetching trip | reference={}, userId={}", reference, userId);
        userService.getUserById(userId);
        return new PlannedTripResponse(plannedTripService.getPlannedTrip(reference));
    }

    public PlannedTripResponse closeTrip(String reference, Long userId) {
        log.info("Closing trip | reference={}, userId={}", reference, userId);
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if (business == null) {
            log.error("User has no business | userId={}", userId);
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        PlannedTrip trip = plannedTripService.update(
                PlannedTripStatus.CLOSED,
                reference,
                business.getCode()
        );

        log.info("Trip closed successfully | reference={}", reference);
        return new PlannedTripResponse(trip);
    }

    @Transactional(rollbackOn = Exception.class)
    public DeliveryRequestCreationResponse acceptTripOffer(String reference, Long userId) {
        log.info("Accepting trip offer | reference={}, userId={}", reference, userId);

        SnapUser user = userService.getUserById(userId);
        PlannedTripOffer offer = tripOfferService.accept(reference, user);

        log.debug("Trip offer accepted | offerRef={}, status={}", offer.getReference(), offer.getStatus());

        DeliveryRequest deliveryRequest = createDeliveryRequestFromTripOffer(user, offer);

        log.info("Delivery request created from offer | trackingId={}", deliveryRequest.getTrackingId());

        return DeliveryRequestCreationResponse.builder()
                .status(deliveryRequest.getStatus())
                .trackId(deliveryRequest.getTrackingId())
                .build();
    }

    private DeliveryRequest createDeliveryRequestFromTripOffer(SnapUser user, PlannedTripOffer offer) {
        log.info("Creating delivery request from trip offer | offerRef={}", offer.getReference());

        DeliveryRequest deliveryRequest = requestService.createRequest(
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
        log.debug("Delivery request assigned to trip | trackingId={}", deliveryRequest.getTrackingId());

        String msg;
        try {
            pendingPaymentService.create(deliveryRequest);
            walletManagementService.startRequestPayment(deliveryRequest);
            pendingPaymentService.markPaid(deliveryRequest);
            msg = "Your trip offer has just been accepted and account credited";
            log.info("Payment completed successfully | trackingId={}", deliveryRequest.getTrackingId());
        } catch (Exception e) {
            log.error("Payment processing failed | trackingId={}", deliveryRequest.getTrackingId(), e);
            msg = "Your trip offer has just been accepted. Awaiting Payment";
        }

        notificationService.send(
                AddAppNotificationDto.builder()
                        .message(msg)
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

        log.info("Delivery request fully processed | trackingId={}", deliveryRequest.getTrackingId());
        return deliveryRequest;
    }
}
