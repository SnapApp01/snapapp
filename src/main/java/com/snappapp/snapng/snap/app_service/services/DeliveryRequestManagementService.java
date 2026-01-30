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
        SnapUser user = userService.getUserById(userId);
        Location pickup = locationService.addLocation(LocationCreationDto.builder()
                .address(request.getPickup().getAddress())
                .city(request.getPickup().getCity().trim())
                .state(request.getPickup().getState())
                .landmark(request.getPickup().getLandMark())
                .latitude(request.getPickup().getLatitude())
                .longitude(request.getPickup().getLongitude())
                .build());
        Location destination = locationService.addLocation(LocationCreationDto.builder()
                .address(request.getDestination().getAddress().trim())
                .city(request.getDestination().getCity().trim())
                .state(request.getDestination().getState())
                .landmark(request.getDestination().getLandMark())
                .latitude(request.getDestination().getLatitude())
                .longitude(request.getDestination().getLongitude())
                .build());

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
                .endTime(Strings.isNullOrEmpty(request.getEnd()) ? DateTimeUtils.parseDateTime(request.getStart())
                        : DateTimeUtils.parseDateTime(request.getEnd()))
                .calculatedFee(checkMinimumCost(request).getCostInLong())
                .build());
        return DeliveryRequestCreationResponse.builder()
                .status(deliveryRequest.getStatus())
                .trackId(deliveryRequest.getTrackingId())
                .build();
    }

    @Async
    public void pushNotificationForNewRequest(String trackingId){
        List<Business> businesses = businessService.getOnlineBusinesses();
        for(Business business : businesses){
            SnapUser snapUser = businessService.getUserOfBusiness(business);
            if(snapUser!=null){
                notificationService.send(AddAppNotificationDto.builder()
                        .message("There is a new delivery request. Check it out and make a bid.")
                        .uid(snapUser.getIdentifier())
                        .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                        .taskId(trackingId)
                        .title(NotificationTitle.DELIVERY)
                        .build());
            }
        }
    }

    public CalculateMinimumCostResponse checkMinimumCost(CreateDeliveryRequest request){
        double distance = GeoUtilities.distanceInKM(request.getPickup().getLatitude(),
                request.getPickup().getLongitude(),request.getDestination().getLatitude(),
                request.getDestination().getLongitude());
        CalculatorParams params = CalculatorParams
                .builder()
                .dieselPrice(80000)
                .fuelPrice(64000)
                .minCap(50000)
                .processorCharges(1.015)
                .vat(0.075)
                .distanceInKm(distance)
                .build();
        MinimumCostCalculator calculator = MinimumCostCalculator.getInstance(request.getVehicle(),params);
        assert calculator != null;
        return CalculateMinimumCostResponse.builder()
                .costInLong(calculator.calculate()).build();
    }

    public Boolean hasPendingRequest(Long userId){
        SnapUser user = userService.getUserById(userId);
        List<DeliveryRequest> requests = deliveryRequestService.getNotConfirmedForUser(user);
        return !requests.isEmpty();
    }

    public List<DeliveryRequestRetrievalResponse> getPendingRequest(Long userId){
        SnapUser user = userService.getUserById(userId);
        List<DeliveryRequest> requests = deliveryRequestService.getPendingRequestsForUser(user);
        return requests.stream().map(e->DeliveryRequestRetrievalResponse.builder().request(e).build()).toList();
    }

    public List<DeliveryRequestRetrievalResponse> getUserRequests(Long userId){
        SnapUser user = userService.getUserById(userId);
        List<DeliveryRequest> requests = deliveryRequestService.getForUser(user);
        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e->responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build()));
        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getAcceptedRequests(Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        List<DeliveryRequest> requests = deliveryRequestService.getForBusiness(business);
        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e->responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build()));
        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getUnAssignedRequests(Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        if(!business.getIsOnline() || !business.getIsVerified()){
            throw new FailedProcessException("You are not currently online and can therefore not get requests.");
        }
        List<DeliveryRequest> requests = deliveryRequestService.getUnAssignedInstant(getVehicleTypes(business));
        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e->responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build()));
        return responses;
    }

    public List<DeliveryRequestRetrievalResponse> getUnAssignedNotInstantRequests(Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        if(!business.getIsOnline() || !business.getIsVerified()){
            throw new FailedProcessException("You are not currently online and can therefore not get requests.");
        }
        List<DeliveryRequest> requests = deliveryRequestService.getUnAssignedNotInstant(getVehicleTypes(business));
        List<DeliveryRequestRetrievalResponse> responses = new ArrayList<>();
        requests.forEach(e->responses.add(DeliveryRequestRetrievalResponse.builder().request(e).build()));
        return responses;
    }

    public DeliveryRequestRetrievalResponse getUserRequest(Long userId, String trackingId) {
        // 1️⃣ Fetch user
        SnapUser user = userService.getUserById(userId);

        // 2️⃣ Fetch delivery request (ensures user owns it)
        DeliveryRequest request = deliveryRequestService.get(trackingId, user);

        // 3️⃣ Determine proposalId
        String proposalId = null;

        if (request.getProposals() != null && !request.getProposals().isEmpty()) {

            // Try to get the accepted proposal first
            Optional<DeliveryPriceProposal> acceptedProposalOpt = request.getProposals().stream()
                    .filter(p -> FeeProposalStatus.ACCEPTED.equals(p.getStatus()))
                    .findFirst();

            if (acceptedProposalOpt.isPresent()) {
                proposalId = acceptedProposalOpt.get().getProposalId();
            } else {
                // Fallback: get the latest proposal based on creation time
                proposalId = request.getProposals().stream()
                        .max(Comparator.comparing(BaseEntity::getCreatedAt)) // assuming BaseEntity has createdAt
                        .map(DeliveryPriceProposal::getProposalId)
                        .orElse(null);
            }
        }

        // 4️⃣ Get pending payment info
        DeliveryRequestPendingPayment pendingPayment = pendingPaymentService.get(request);

        // 5️⃣ Build and return response
        DeliveryRequestRetrievalResponse.DeliveryRequestRetrievalResponseBuilder builder =
                DeliveryRequestRetrievalResponse.builder()
                        .request(request);

        if (pendingPayment != null) {
            builder.expiryTimeForPayment(pendingPayment.getExpiryTime());
        }

        return builder.build();
    }


//    public DeliveryRequestRetrievalResponse getUserRequest(Long userId, String trackingId){
//        SnapUser user = userService.getUserById(userId);
//        DeliveryRequest request = deliveryRequestService.get(trackingId,user);
//        DeliveryRequestPendingPayment pendingPayment = pendingPaymentService.get(request);
//        if(pendingPayment!=null){
//            return DeliveryRequestRetrievalResponse.builder()
//                    .expiryTimeForPayment(pendingPayment.getExpiryTime())
//                    .request(request).build();
//        }
//        // get accepted proposal directly
//        DeliveryPriceProposal acceptedProposal = request.getProposals().stream()
//                .filter(p -> p.getStatus() == FeeProposalStatus.ACCEPTED)
//                .findFirst()
//                .orElse(request.getProposals().stream()
//                        .max(Comparator.comparing(BaseEntity::getCreatedAt))
//                        .orElse(null));
//
//        String proposalId = acceptedProposal != null ? acceptedProposal.getProposalId() : null;
//
//        return DeliveryRequestRetrievalResponse.builder().request(request).proposalId(proposalId).build();
//    }

    public DeliveryRequestRetrievalResponse completeDelivery(Long userId, String trackingId){
        SnapUser user = userService.getUserById(userId);
        DeliveryRequest request = deliveryRequestService.get(trackingId,user);
        if(DeliveryRequestStatus.DELIVERED.equals(request.getStatus())) {
            WalletTransfer transfer = walletTransferService.getTransferWithExternalRef(request.getTrackingId());
            walletTransferService.completeTransfer(transfer.getTransferRefId());
            request = deliveryRequestService.updateStatus(trackingId, DeliveryRequestStatus.COMPLETED);

            notificationService.send(AddAppNotificationDto.builder()
                    .message("Delivery has been completed, funds have been released")
                    .uid(request.getBusinessUserId())
                    .task(NotificationTask.RIDER_DELIVERY.name())
                    .taskId(request.getTrackingId())
                    .title(NotificationTitle.DELIVERY)
                    .build());
        }
        return DeliveryRequestRetrievalResponse.builder().request(request).build();
    }

    public DeliveryRequestRetrievalResponse cancelDelivery(Long userId, String trackingId){
        SnapUser user = userService.getUserById(userId);
        DeliveryRequest request = deliveryRequestService.get(trackingId,user);
        if(DeliveryRequestStatus.NEW.equals(request.getStatus()) || DeliveryRequestStatus.AWAITING_PAYMENT.equals(request.getStatus())){
            request = deliveryRequestService.updateStatus(trackingId,DeliveryRequestStatus.CANCELED);
            if(!Strings.isNullOrEmpty(request.getBusinessUserId())) {
                notificationService.send(AddAppNotificationDto.builder()
                        .message("Delivery request has been canceled by user.")
                        .uid(request.getBusinessUserId())
                        .title(NotificationTitle.DELIVERY)
                        .build());
            }
            else{
                notifyBiddersOfCancellation(request);
            }
            return DeliveryRequestRetrievalResponse.builder().request(request).build();
        }
        throw new FailedProcessException("This request cannot be canceled again as it had been paid for. Kindly contact support");
    }

    private void notifyBiddersOfCancellation(DeliveryRequest request){
        List<DeliveryPriceProposal> proposals = proposalService.getProposals(request);
        for(DeliveryPriceProposal proposal : proposals){
            notificationService.send(AddAppNotificationDto.builder()
                    .message("Your bid has been canceled as order is no longer available")
                    .uid(proposal.getBusinessUserId())
                    .title(NotificationTitle.DELIVERY)
                    .build());
        }
    }

    public DeliveryRequestRetrievalResponse getRequestByBusiness(Long userId, String trackingId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        DeliveryRequest request = deliveryRequestService.get(trackingId);
        if(!DeliveryRequestStatus.NEW.equals(request.getStatus())){
            if(request.getBusiness().getId().longValue()!= business.getId().longValue()){
                throw new ResourceNotFoundException("This request is no longer available to other riders");
            }
        }
        else{
            if(!business.getIsOnline() || !business.getIsVerified()){
                throw new FailedProcessException("You are not currently online and can therefore not get requests.");
            }
        }
        return DeliveryRequestRetrievalResponse.builder().request(request).build();
    }

    public DeliveryRequestRetrievalResponse updateRequestStatusByBusiness(Long userId, String trackingId){
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        DeliveryRequest request = deliveryRequestService.get(trackingId,business);
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
                throw new FailedProcessException("Process is not permitted for you");
        }
        request = deliveryRequestService.updateStatus(trackingId,request.getStatus());
        notificationService.send(AddAppNotificationDto.builder()
                        .title(NotificationTitle.DELIVERY)
                        .uid(request.getUser().getIdentifier())
                        .message(message)
                        .taskId(request.getTrackingId())
                        .task(NotificationTask.USER_DELIVERY.name())
                .build());
        return DeliveryRequestRetrievalResponse.builder().request(request).build();
    }

    private List<VehicleType> getVehicleTypes(Business business){
        Set<VehicleType> types = new HashSet<>();
        vehicleService.getVehiclesForBusiness(business).forEach(e->{
            types.add(e.getType());
        });
        return new ArrayList<>(types);
    }
}
