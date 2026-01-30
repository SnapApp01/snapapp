package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.InsufficientBalanceException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.app_service.apimodels.CreatePriceProposalRequest;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryPriceProposalResponse;
import com.snappapp.snapng.snap.app_service.apimodels.UpdatePriceProposalRequest;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.dtos.PriceProposalCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTask;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestRepository;
import com.snappapp.snapng.snap.data_lib.service.*;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PriceManagementService {
    private final SnapUserService userService;
    private final BusinessService businessService;
    private final DeliveryPriceProposalService proposalService;
    private final DeliveryRequestService deliveryRequestService;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final VehicleService vehicleService;
    private final WalletManagementService walletManagementService;
    private final PushNotificationService notificationService;
    private final DeliveryRequestPendingPaymentService pendingPaymentService;

    public PriceManagementService(SnapUserService userService, BusinessService businessService, DeliveryPriceProposalService proposalService, DeliveryRequestService deliveryRequestService, DeliveryRequestRepository deliveryRequestRepository, VehicleService vehicleService, WalletManagementService walletManagementService, PushNotificationService notificationService, DeliveryRequestPendingPaymentService pendingPaymentService) {
        this.userService = userService;
        this.businessService = businessService;
        this.proposalService = proposalService;
        this.deliveryRequestService = deliveryRequestService;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.vehicleService = vehicleService;
        this.walletManagementService = walletManagementService;
        this.notificationService = notificationService;
        this.pendingPaymentService = pendingPaymentService;
    }

    public List<DeliveryPriceProposalResponse> getPriceProposals(String trackingId, Long userId){
        SnapUser user = userService.getUserById(userId);
        DeliveryRequest request = deliveryRequestService.get(trackingId,user);
        List<DeliveryPriceProposal> priceProposals = proposalService.getProposals(request);
        List<DeliveryPriceProposalResponse> responses = new ArrayList<>();
        int size = Math.min(priceProposals.size(),5);
        for(int i= 0; i<size;i++){
            responses.add(new DeliveryPriceProposalResponse(priceProposals.get(i)));
        }
        //priceProposals.forEach(e->responses.add(new DeliveryPriceProposalResponse(e)));
        return responses;
    }

    public DeliveryPriceProposalResponse getPriceProposalByBusiness(String trackingId, Long userId) {

        SnapUser user = userService.getUserById(userId);

        Business business = businessService.getBusinessOfUser(user);
        if (business == null) {
            throw new ResourceNotFoundException("There is no business owned by this user");
        }

        DeliveryRequest request = deliveryRequestService.get(trackingId);
        if (request == null) {
            throw new ResourceNotFoundException("Delivery request not found");
        }

        DeliveryPriceProposal proposal =
                proposalService.getProposal(request, business);

        if (proposal == null) {
            throw new ResourceNotFoundException(
                    "No price proposal found for this business"
            );
        }

        return new DeliveryPriceProposalResponse(proposal);
    }


//    public DeliveryPriceProposalResponse getPriceProposalByBusiness(String trackingId, Long userId){
//        SnapUser user = userService.getUserById(userId);
//        Business business = businessService.getBusinessOfUser(user);
//        if(business==null){
//            throw new ResourceNotFoundException("There is no business owned to this user");
//        }
//        DeliveryRequest request = deliveryRequestService.get(trackingId);
//        DeliveryPriceProposal proposal = proposalService.getProposal(request,business);
//        return new DeliveryPriceProposalResponse(proposal);
//    }

    public DeliveryPriceProposalResponse addPriceProposal(Long userId, CreatePriceProposalRequest request){
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("There is no business owned to this user");
        }
        DeliveryRequest deliveryRequest = deliveryRequestService.get(request.getTrackId());
        if(deliveryRequest.getStatus()!= DeliveryRequestStatus.NEW && deliveryRequest.getBusiness()!=null){
            throw new FailedProcessException("Delivery request is no longer available");
        }
        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
        if(!vehicle.getBusiness().getId().equals(business.getId())){
            throw new FailedProcessException("Vehicle is not owned by this business");
        }
        if(!vehicle.getType().equals(deliveryRequest.getVehicleType())){
            throw new FailedProcessException("Vehicle is not same as requested by customer");
        }
        DeliveryPriceProposal proposal = proposalService.createProposal(PriceProposalCreationDto.builder()
                .amount(MoneyUtilities.fromDoubleToMinor(request.getProposedFee()))
                .businessInitiated(true)
                .comment(request.getComment())
                .request(deliveryRequest)
                .vehicle(vehicle)
                .build());
        notificationService.send(AddAppNotificationDto.builder()
                .message("You have proposals for your delivery request")
                .title(NotificationTitle.DELIVERY)
                .uid(deliveryRequest.getUser().getIdentifier())
                .task(NotificationTask.USER_DELIVERY.name())
                .taskId(deliveryRequest.getTrackingId())
                .build());
        return new DeliveryPriceProposalResponse(proposal);
    }

    public DeliveryPriceProposalResponse updateProposal(Long userId, UpdatePriceProposalRequest request){
        SnapUser user = userService.getUserById(userId);
        DeliveryPriceProposal proposal = proposalService.getProposal(request.getProposalId(),user);
        if(request.isAccept() && deliveryRequestService.vehicleActiveRequest(proposal.getVehicle())){
            proposalService.updateProposal(request.getProposalId(), false);
            throw new FailedProcessException("Sorry, the vehicle is no longer available for your request");
        }
        proposal = proposalService.updateProposal(request.getProposalId(),request.isAccept());
        if(request.isAccept()) {
            DeliveryRequest deliveryRequest = deliveryRequestService.assignToVehicleWithProposal(proposal);
            pendingPaymentService.create(deliveryRequest);
            String msg;
            try {
                walletManagementService.startRequestPayment(proposal.getRequest());
                pendingPaymentService.markPaid(deliveryRequest);
                msg = "Your bid has just been accepted and account credited";
            }catch (Exception e){
                msg = "Your bid has just been accepted. Awaiting Payment";
            }
            notificationService.send(AddAppNotificationDto.builder()
                    .message(msg)
                    .title(NotificationTitle.DELIVERY)
                    .uid(deliveryRequest.getBusinessUserId())
                    .task(NotificationTask.RIDER_DELIVERY.name())
                    .taskId(deliveryRequest.getTrackingId())
                    .build());
        }
        else{
            proposalService.updateCounterProposal(request.getProposalId(), request.getCounterProposal());
            notificationService.send(AddAppNotificationDto.builder()
                    .message("Your bid was not accepted, please submit another bid. " +
                            "Client proposes this amount " + proposal.getCounterProposal())
                    .title(NotificationTitle.DELIVERY)
                    .uid(proposal.getBusinessUserId())
                            .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                            .taskId(proposal.getRequest().getTrackingId())
                    .build());
        }
        return new DeliveryPriceProposalResponse(proposal);
    }

    @Async
    public void sendNotificationToOtherProposals(UpdatePriceProposalRequest request, Long userId){
        if(request.isAccept()){
            SnapUser user = userService.getUserById(userId);
            DeliveryPriceProposal proposal = proposalService.getProposal(request.getProposalId(),user);
            DeliveryRequest deliveryRequest = proposal.getRequest();
            List<DeliveryPriceProposal> pendingProposals = proposalService.getProposals(deliveryRequest);
            for(DeliveryPriceProposal p : pendingProposals){
                notificationService.send(AddAppNotificationDto.builder()
                        .message("Your bid was not successful, order is no longer available.")
                        .title(NotificationTitle.DELIVERY)
                        .uid(p.getBusinessUserId())
                        .build());
            }
        }
    }

    public void processPayment(String trackId){
        DeliveryRequest request = deliveryRequestService.get(trackId);
        walletManagementService.startRequestPayment(request);
    }

    @Transactional
    public DeliveryPriceProposalResponse acceptProposal(Long userId, String proposalId) {

        SnapUser user = userService.getUserById(userId);
        DeliveryPriceProposal proposal =
                proposalService.getProposal(proposalId, user);

        // 1️⃣ Check vehicle availability
        if (deliveryRequestService.vehicleActiveRequest(proposal.getVehicle())) {
            throw new FailedProcessException(
                    "Sorry, the vehicle is no longer available for your request"
            );
        }

        DeliveryRequest request = proposal.getRequest();
        request.setAgreedFee(proposal.getFee());
        deliveryRequestRepository.save(request);

        // 2️⃣ 🔥 CHECK WALLET BALANCE FIRST (NO DEBIT)
        if (user.getBalance().compareTo(proposal.getFee()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        // 3️⃣ Accept proposal
        proposalService.updateProposal(proposalId, true);

        // 4️⃣ Assign rider ONLY after wallet check
        DeliveryRequest deliveryRequest =
                deliveryRequestService.assignToVehicleWithProposal(proposal);

        // 5️⃣ Create pending payment
        pendingPaymentService.create(deliveryRequest);

        // 6️⃣ Take payment
        walletManagementService.startRequestPayment(deliveryRequest);

        pendingPaymentService.markPaid(deliveryRequest);

        // 7️⃣ Notify
        notificationService.send(AddAppNotificationDto.builder()
                .message("Your bid has just been accepted and payment completed")
                .title(NotificationTitle.DELIVERY)
                .uid(deliveryRequest.getBusinessUserId())
                .task(NotificationTask.RIDER_DELIVERY.name())
                .taskId(deliveryRequest.getTrackingId())
                .build());

        return new DeliveryPriceProposalResponse(proposal);
    }

    @Transactional
    public DeliveryPriceProposalResponse rejectProposal(
            Long userId,
            String proposalId,
            Long counterProposal
    ) {

        SnapUser user = userService.getUserById(userId);
        DeliveryPriceProposal proposal =
                proposalService.getProposal(proposalId, user);

        // Reject proposal
        proposalService.updateProposal(proposalId, false);

        // Set counter proposal
        proposalService.updateCounterProposal(proposalId, counterProposal);

        // Notify rider
        notificationService.send(AddAppNotificationDto.builder()
                .message(
                        "Your bid was not accepted. Client proposes this amount " +
                                counterProposal
                )
                .title(NotificationTitle.DELIVERY)
                .uid(proposal.getBusinessUserId())
                .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                .taskId(proposal.getRequest().getTrackingId())
                .build());

        return new DeliveryPriceProposalResponse(proposal);
    }

}
