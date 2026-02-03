package com.snappapp.snapng.snap.app_service.services;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.app_service.apimodels.CalculateMinimumCostResponse;
import com.snappapp.snapng.snap.app_service.apimodels.CreateDeliveryRequest;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryRequestCreationResponse;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryRequestRetrievalResponse;
import com.snappapp.snapng.snap.app_service.components.CalculatorParams;
import com.snappapp.snapng.snap.app_service.components.MinimumCostCalculator;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.dtos.LocationCreationDto;
import com.snappapp.snapng.snap.data_lib.dtos.RequestCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.*;
import com.snappapp.snapng.snap.data_lib.service.*;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import com.snappapp.snapng.snap.utils.utilities.GeoUtilities;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DeliveryRequestManagementService {

    private final SnapUserService userService;
    private final BusinessService businessService;
    private final DeliveryRequestService deliveryRequestService;
    private final DeliveryPriceProposalService proposalService;
    private final LocationService locationService;
    private final VehicleService vehicleService;
    private final WalletTransferService walletTransferService;
    private final PushNotificationService notificationService;
    private final DeliveryRequestPendingPaymentService pendingPaymentService;

    public DeliveryRequestManagementService(SnapUserService userService, BusinessService businessService, DeliveryRequestService deliveryRequestService, DeliveryPriceProposalService proposalService, LocationService locationService, VehicleService vehicleService, WalletTransferService walletTransferService, PushNotificationService notificationService, DeliveryRequestPendingPaymentService pendingPaymentService) {
        this.userService = userService;
        this.businessService = businessService;
        this.deliveryRequestService = deliveryRequestService;
        this.proposalService = proposalService;
        this.locationService = locationService;
        this.vehicleService = vehicleService;
        this.walletTransferService = walletTransferService;
        this.notificationService = notificationService;
        this.pendingPaymentService = pendingPaymentService;
    }

    public DeliveryRequestCreationResponse create(Long userId, CreateDeliveryRequest request){

        log.debug("Creating delivery request for userId={}", userId);

        SnapUser user = userService.getUserById(userId);

        log.debug("User resolved for create request: id={}, identifier={}",
                user.getId(), user.getIdentifier());

        Location pickup = locationService.addLocation(LocationCreationDto.builder()
                .address(request.getPickup().getAddress())
                .city(request.getPickup().getCity().trim())
                .state(request.getPickup().getState())
                .landmark(request.getPickup().getLandMark())
                .latitude(request.getPickup().getLatitude())
                .longitude(request.getPickup().getLongitude())
                .build());

        log.debug("Pickup location created: id={}", pickup.getId());

        Location destination = locationService.addLocation(LocationCreationDto.builder()
                .address(request.getDestination().getAddress().trim())
                .city(request.getDestination().getCity().trim())
                .state(request.getDestination().getState())
                .landmark(request.getDestination().getLandMark())
                .latitude(request.getDestination().getLatitude())
                .longitude(request.getDestination().getLongitude())
                .build());

        log.debug("Destination location created: id={}", destination.getId());

        long calculatedFee = checkMinimumCost(request).getCostInLong();

        log.debug("Calculated delivery fee={} (minor units)", calculatedFee);

        DeliveryRequest deliveryRequest = deliveryRequestService.createRequest(RequestCreationDto
                .builder()
                .additionalNote(request.getNote().trim())
                .deliveryFrequency(request.getFrequency())
                .description(request.getDescription())
                .destinationLocation(destination)
                .pickupLocation(pickup)
                .recipientName(request.getRecipient())
                .weight(request.getWeight())
                .recipientNumber(request.getRecipientNumber())
                .user(user)
                .sendType(request.getSendType())
                .vehicleType(request.getVehicle())
                .worth(MoneyUtilities.fromDoubleToMinor(request.getWorth()))
                .startTime(DateTimeUtils.parseDateTime(request.getStart()))
                .endTime(Strings.isNullOrEmpty(request.getEnd())
                        ? DateTimeUtils.parseDateTime(request.getStart())
                        : DateTimeUtils.parseDateTime(request.getEnd()))
                .calculatedFee(calculatedFee)
                .build());

        log.info("Delivery request created: trackingId={}, user={}",
                deliveryRequest.getTrackingId(), user.getIdentifier());

        return DeliveryRequestCreationResponse.builder()
                .status(deliveryRequest.getStatus())
                .trackId(deliveryRequest.getTrackingId())
                .build();
    }

    @Async
    public void pushNotificationForNewRequest(String trackingId){

        log.debug("Pushing notifications for new delivery request trackingId={}", trackingId);

        List<Business> businesses = businessService.getOnlineBusinesses();

        log.debug("Found {} online businesses for broadcast", businesses.size());

        for(Business business : businesses){

            SnapUser snapUser = businessService.getUserOfBusiness(business);

            if(snapUser!=null){

                log.debug("Sending new request notification to business user={}, businessId={}",
                        snapUser.getIdentifier(), business.getId());

                notificationService.send(AddAppNotificationDto.builder()
                        .message("There is a new delivery request. Check it out and make a bid.")
                        .uid(snapUser.getIdentifier())
                        .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                        .taskId(trackingId)
                        .title(NotificationTitle.DELIVERY)
                        .build());
            }else{
                log.warn("No user found for businessId={} while sending new request notification",
                        business.getId());
            }
        }
    }

    public CalculateMinimumCostResponse checkMinimumCost(CreateDeliveryRequest request){

        double distance = GeoUtilities.distanceInKM(
                request.getPickup().getLatitude(),
                request.getPickup().getLongitude(),
                request.getDestination().getLatitude(),
                request.getDestination().getLongitude());

        log.debug("Distance calculated for cost computation: {} km", distance);

        CalculatorParams params = CalculatorParams
                .builder()
                .dieselPrice(80000)
                .fuelPrice(64000)
                .minCap(50000)
                .processorCharges(1.015)
                .vat(0.075)
                .distanceInKm(distance)
                .build();

        MinimumCostCalculator calculator =
                MinimumCostCalculator.getInstance(request.getVehicle(), params);

        if(calculator == null){
            log.warn("No minimum cost calculator found for vehicle type={}",
                    request.getVehicle());
        }

        long cost = calculator.calculate();

        log.debug("Minimum cost calculated={}", cost);

        return CalculateMinimumCostResponse.builder()
                .costInLong(cost)
                .build();
    }

    public Boolean hasPendingRequest(Long userId){

        log.debug("Checking pending requests for userId={}", userId);

        SnapUser user = userService.getUserById(userId);

        List<DeliveryRequest> requests =
                deliveryRequestService.getNotConfirmedForUser(user);

        log.debug("User {} has {} pending (not confirmed) requests",
                user.getIdentifier(), requests.size());

        return !requests.isEmpty();
    }

    public List<DeliveryRequestRetrievalResponse> getPendingRequest(Long userId){

        log.debug("Fetching pending requests for userId={}", userId);

        SnapUser user = userService.getUserById(userId);

        List<DeliveryRequest> requests =
                deliveryRequestService.getPendingRequestsForUser(user);

        log.debug("Found {} pending requests for user={}",
                requests.size(), user.getIdentifier());

        return requests.stream()
                .map(e -> DeliveryRequestRetrievalResponse.builder().request(e).build())
                .toList();
    }

    public List<DeliveryRequestRetrievalResponse> getUserRequests(Long userId){

        log.debug("Fetching all requests for userId={}", userId);

        SnapUser user = userService.getUserById(userId);

        List<DeliveryRequest> requests = deliveryRequestService.getForUser(user);

        log.debug("Found {} total requests for user={}",
                requests.size(), user.getIdentifier());

        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e ->
                responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build())
        );

        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getAcceptedRequests(Long userId) {

        log.debug("Fetching accepted requests for business userId={}", userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business==null){
            log.warn("User {} does not own any business", user.getIdentifier());
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        List<DeliveryRequest> requests =
                deliveryRequestService.getForBusiness(business);

        log.debug("Found {} requests for businessId={}",
                requests.size(), business.getId());

        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e ->
                responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build())
        );

        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getUnAssignedRequests(Long userId) {

        log.debug("Fetching unassigned instant requests for business userId={}", userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business==null){
            log.warn("User {} does not own a business", user.getIdentifier());
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        if(!business.getIsOnline() || !business.getIsVerified()){
            log.warn("Business {} is not eligible (online={}, verified={})",
                    business.getId(), business.getIsOnline(), business.getIsVerified());
            throw new FailedProcessException("You are not currently online and can therefore not get requests.");
        }

        List<VehicleType> types = getVehicleTypes(business);

        log.debug("Vehicle types for businessId={} -> {}", business.getId(), types);

        List<DeliveryRequest> requests =
                deliveryRequestService.getUnAssignedInstant(types);

        log.debug("Found {} unassigned instant requests", requests.size());

        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e ->
                responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build())
        );

        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getUnAssignedNotInstantRequests(Long userId) {

        log.debug("Fetching unassigned non-instant requests for business userId={}", userId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business==null){
            log.warn("User {} does not own a business", user.getIdentifier());
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        if(!business.getIsOnline() || !business.getIsVerified()){
            log.warn("Business {} is not eligible (online={}, verified={})",
                    business.getId(), business.getIsOnline(), business.getIsVerified());
            throw new FailedProcessException("You are not currently online and can therefore not get requests.");
        }

        List<VehicleType> types = getVehicleTypes(business);

        log.debug("Vehicle types for businessId={} -> {}", business.getId(), types);

        List<DeliveryRequest> requests =
                deliveryRequestService.getUnAssignedNotInstant(types);

        log.debug("Found {} unassigned non-instant requests", requests.size());

        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e ->
                responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build())
        );

        return responses;
    }

    public DeliveryRequestRetrievalResponse getUserRequest(Long userId, String trackingId) {

        log.debug("Fetching user request. userId={}, trackingId={}", userId, trackingId);

        SnapUser user = userService.getUserById(userId);

        DeliveryRequest request = deliveryRequestService.get(trackingId, user);

        log.debug("Delivery request resolved id={}, status={}",
                request.getId(), request.getStatus());

        String proposalId = null;

        if (request.getProposals() != null && !request.getProposals().isEmpty()) {

            log.debug("Request {} has {} proposals",
                    trackingId, request.getProposals().size());

            Optional<DeliveryPriceProposal> acceptedProposalOpt =
                    request.getProposals().stream()
                            .filter(p -> FeeProposalStatus.ACCEPTED.equals(p.getStatus()))
                            .findFirst();

            if (acceptedProposalOpt.isPresent()) {
                proposalId = acceptedProposalOpt.get().getProposalId();
                log.debug("Accepted proposal found: {}", proposalId);
            } else {
                proposalId = request.getProposals().stream()
                        .max(Comparator.comparing(BaseEntity::getCreatedAt))
                        .map(DeliveryPriceProposal::getProposalId)
                        .orElse(null);

                log.debug("Latest proposal used instead: {}", proposalId);
            }
        } else {
            log.debug("Request {} has no proposals", trackingId);
        }

        DeliveryRequestPendingPayment pendingPayment =
                pendingPaymentService.get(request);

        if(pendingPayment != null){
            log.debug("Pending payment found for request {}, expiry={}",
                    trackingId, pendingPayment.getExpiryTime());
        }else{
            log.debug("No pending payment found for request {}", trackingId);
        }

        DeliveryRequestRetrievalResponse.DeliveryRequestRetrievalResponseBuilder builder =
                DeliveryRequestRetrievalResponse.builder()
                        .request(request);

        if (pendingPayment != null) {
            builder.expiryTimeForPayment(pendingPayment.getExpiryTime());
        }

        return builder.build();
    }

    public DeliveryRequestRetrievalResponse completeDelivery(Long userId, String trackingId){

        log.info("Completing delivery request. userId={}, trackingId={}",
                userId, trackingId);

        SnapUser user = userService.getUserById(userId);
        DeliveryRequest request = deliveryRequestService.get(trackingId, user);

        log.debug("Current request status={}", request.getStatus());

        if(DeliveryRequestStatus.DELIVERED.equals(request.getStatus())) {

            WalletTransfer transfer =
                    walletTransferService.getTransferWithExternalRef(
                            request.getTrackingId());

            log.debug("Completing wallet transfer transferRef={}",
                    transfer.getTransferRefId());

            walletTransferService.completeTransfer(transfer.getTransferRefId());

            request = deliveryRequestService.updateStatus(
                    trackingId, DeliveryRequestStatus.COMPLETED);

            log.info("Delivery completed. trackingId={}", trackingId);

            notificationService.send(AddAppNotificationDto.builder()
                    .message("Delivery has been completed, funds have been released")
                    .uid(request.getBusinessUserId())
                    .task(NotificationTask.RIDER_DELIVERY.name())
                    .taskId(request.getTrackingId())
                    .title(NotificationTitle.DELIVERY)
                    .build());
        }

        return DeliveryRequestRetrievalResponse.builder()
                .request(request)
                .build();
    }

    public DeliveryRequestRetrievalResponse cancelDelivery(Long userId, String trackingId){

        log.info("Cancel delivery request. userId={}, trackingId={}",
                userId, trackingId);

        SnapUser user = userService.getUserById(userId);
        DeliveryRequest request = deliveryRequestService.get(trackingId, user);

        log.debug("Current request status={}", request.getStatus());

        if(DeliveryRequestStatus.NEW.equals(request.getStatus())
                || DeliveryRequestStatus.AWAITING_PAYMENT.equals(request.getStatus())){

            request = deliveryRequestService.updateStatus(
                    trackingId, DeliveryRequestStatus.CANCELED);

            log.info("Delivery request canceled. trackingId={}", trackingId);

            if(!Strings.isNullOrEmpty(request.getBusinessUserId())) {

                log.debug("Notifying business user={} of cancellation",
                        request.getBusinessUserId());

                notificationService.send(AddAppNotificationDto.builder()
                        .message("Delivery request has been canceled by user.")
                        .uid(request.getBusinessUserId())
                        .title(NotificationTitle.DELIVERY)
                        .build());
            }
            else{
                log.debug("No assigned business, notifying all bidders of cancellation");
                notifyBiddersOfCancellation(request);
            }

            return DeliveryRequestRetrievalResponse.builder()
                    .request(request)
                    .build();
        }

        log.warn("Cancel not allowed for request {} in status {}",
                trackingId, request.getStatus());

        throw new FailedProcessException(
                "This request cannot be canceled again as it had been paid for. Kindly contact support");
    }

    private void notifyBiddersOfCancellation(DeliveryRequest request){

        List<DeliveryPriceProposal> proposals =
                proposalService.getProposals(request);

        log.debug("Notifying {} bidders for canceled request {}",
                proposals.size(), request.getTrackingId());

        for(DeliveryPriceProposal proposal : proposals){

            notificationService.send(AddAppNotificationDto.builder()
                    .message("Your bid has been canceled as order is no longer available")
                    .uid(proposal.getBusinessUserId())
                    .title(NotificationTitle.DELIVERY)
                    .build());
        }
    }

    public DeliveryRequestRetrievalResponse getRequestByBusiness(Long userId, String trackingId) {

        log.debug("Business fetching request. userId={}, trackingId={}",
                userId, trackingId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business==null){
            log.warn("User {} does not own a business", user.getIdentifier());
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        DeliveryRequest request = deliveryRequestService.get(trackingId);

        log.debug("Request resolved id={}, status={}",
                request.getId(), request.getStatus());

        if(!DeliveryRequestStatus.NEW.equals(request.getStatus())){
            if(request.getBusiness().getId().longValue()
                    != business.getId().longValue()){

                log.warn("Business {} tried to access request {} owned by {}",
                        business.getId(), trackingId,
                        request.getBusiness().getId());

                throw new ResourceNotFoundException(
                        "This request is no longer available to other riders");
            }
        }
        else{
            if(!business.getIsOnline() || !business.getIsVerified()){
                log.warn("Business {} is not eligible (online={}, verified={})",
                        business.getId(), business.getIsOnline(),
                        business.getIsVerified());
                throw new FailedProcessException(
                        "You are not currently online and can therefore not get requests.");
            }
        }

        return DeliveryRequestRetrievalResponse.builder()
                .request(request)
                .build();
    }

    public DeliveryRequestRetrievalResponse updateRequestStatusByBusiness(Long userId, String trackingId){

        log.info("Business updating request status. userId={}, trackingId={}",
                userId, trackingId);

        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        if(business==null){
            log.warn("User {} does not own a business", user.getIdentifier());
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        DeliveryRequest request =
                deliveryRequestService.get(trackingId, business);

        log.debug("Current status of request {} is {}",
                trackingId, request.getStatus());

        String message;

        switch (request.getStatus()){
            case AWAITING_PICKUP:
                request.setStatus(DeliveryRequestStatus.ENROUTE);
                message = "Rider has picked up your package";
                break;

            case ENROUTE:
                request.setStatus(DeliveryRequestStatus.DELIVERED);
                message = "Rider has delivered your package";
                break;

            default:
                log.warn("Illegal status transition attempted for request {} with status {}",
                        trackingId, request.getStatus());
                throw new FailedProcessException("Process is not permitted for you");
        }

        request = deliveryRequestService.updateStatus(
                trackingId, request.getStatus());

        log.info("Request {} status updated to {} by business {}",
                trackingId, request.getStatus(), business.getId());

        notificationService.send(AddAppNotificationDto.builder()
                .title(NotificationTitle.DELIVERY)
                .uid(request.getUser().getIdentifier())
                .message(message)
                .taskId(request.getTrackingId())
                .task(NotificationTask.USER_DELIVERY.name())
                .build());

        return DeliveryRequestRetrievalResponse.builder()
                .request(request)
                .build();
    }

    private List<VehicleType> getVehicleTypes(Business business){

        log.debug("Resolving vehicle types for businessId={}", business.getId());

        Set<VehicleType> types = new HashSet<>();

        vehicleService.getVehiclesForBusiness(business).forEach(e -> {
            log.debug("Vehicle found id={}, type={}", e.getId(), e.getType());
            types.add(e.getType());
        });

        return new ArrayList<>(types);
    }
}
