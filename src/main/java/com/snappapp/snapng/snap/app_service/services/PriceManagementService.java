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
    private final WalletService walletService;

    public PriceManagementService(SnapUserService userService, BusinessService businessService, DeliveryPriceProposalService proposalService, DeliveryRequestService deliveryRequestService, DeliveryRequestRepository deliveryRequestRepository, VehicleService vehicleService, WalletManagementService walletManagementService, PushNotificationService notificationService, DeliveryRequestPendingPaymentService pendingPaymentService, WalletService walletService) {
        this.userService = userService;
        this.businessService = businessService;
        this.proposalService = proposalService;
        this.deliveryRequestService = deliveryRequestService;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.vehicleService = vehicleService;
        this.walletManagementService = walletManagementService;
        this.notificationService = notificationService;
        this.pendingPaymentService = pendingPaymentService;
        this.walletService = walletService;
    }

    @Transactional
    public List<DeliveryPriceProposalResponse> getPriceProposals(
            String trackingId,
            Long userId
    ) {

        SnapUser user = userService.getUserById(userId);

        DeliveryRequest request =
                deliveryRequestService.get(trackingId, user);

        List<DeliveryPriceProposal> priceProposals =
                proposalService.getProposals(request);

        List<DeliveryPriceProposalResponse> responses = new ArrayList<>();

        int size = Math.min(priceProposals.size(), 5);

        for (int i = 0; i < size; i++) {

            DeliveryPriceProposal proposal = priceProposals.get(i);

            Vehicle vehicle =
                    vehicleService.getVehicleById(
                            proposal.getVehicleId());

            responses.add(
                    new DeliveryPriceProposalResponse(
                            proposal,
                            request,   // same request for all proposals
                            vehicle
                    )
            );
        }

        return responses;
    }

@Transactional
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

        DeliveryRequest deliveryRequest =
                deliveryRequestService.getDeliveryRequestById(
                        proposal.getDeliveryRequestId());

        Vehicle vehicle =
                vehicleService.getVehicleById(
                        proposal.getVehicleId());

        return new DeliveryPriceProposalResponse(
                proposal,
                deliveryRequest,
                vehicle
        );

    }

    @Transactional
    public DeliveryPriceProposalResponse addPriceProposal(Long userId,
                                                          CreatePriceProposalRequest request){

        SnapUser user = userService.getUserById(userId);

        Business business = businessService.getBusinessOfUser(user);
        if (business == null) {
            throw new ResourceNotFoundException("There is no business owned to this user");
        }

        DeliveryRequest deliveryRequest =
                deliveryRequestService.get(request.getTrackId());

        if (deliveryRequest.getStatus() != DeliveryRequestStatus.NEW
                && deliveryRequest.getBusiness() != null) {
            throw new FailedProcessException("Delivery request is no longer available");
        }

        Vehicle vehicle =
                vehicleService.getVehicleById(request.getVehicleId());

        // ownership check (NEW WAY)
        if (!vehicle.getBusinessId().equals(business.getId())) {
            throw new FailedProcessException("Vehicle is not owned by this business");
        }

        if (!vehicle.getType().equals(deliveryRequest.getVehicleType())) {
            throw new FailedProcessException("Vehicle is not same as requested by customer");
        }

        DeliveryPriceProposal proposal =
                proposalService.createProposal(
                        PriceProposalCreationDto.builder()
                                .amount(MoneyUtilities.fromDoubleToMinor(
                                        request.getProposedFee()))
                                .businessInitiated(true)
                                .comment(request.getComment())
                                .requestId(deliveryRequest.getId())
                                .vehicleId(vehicle.getId())
                                .build()
                );

        notificationService.send(
                AddAppNotificationDto.builder()
                        .message("You have proposals for your delivery request")
                        .title(NotificationTitle.DELIVERY)
                        .uid(deliveryRequest.getUser().getIdentifier())
                        .task(NotificationTask.USER_DELIVERY.name())
                        .taskId(deliveryRequest.getTrackingId())
                        .build()
        );
        return new DeliveryPriceProposalResponse(
                proposal,
                deliveryRequest,
                vehicle
        );

    }

    @Transactional
    public DeliveryPriceProposalResponse updateProposal(Long userId,
                                                        UpdatePriceProposalRequest request){

        SnapUser user = userService.getUserById(userId);

        DeliveryPriceProposal proposal =
                proposalService.getProposal(request.getProposalId(), user);

        Vehicle vehicle = vehicleService.getVehicleById(proposal.getId());

        if (request.isAccept()
                && deliveryRequestService
                .vehicleActiveRequest(vehicle)) {

            proposalService.updateProposal(request.getProposalId(), false);

            throw new FailedProcessException(
                    "Sorry, the vehicle is no longer available for your request");
        }

        proposal =
                proposalService.updateProposal(
                        request.getProposalId(),
                        request.isAccept()
                );

        if (request.isAccept()) {

            DeliveryRequest deliveryRequest =
                    deliveryRequestService.assignToVehicleWithProposal(proposal);

            pendingPaymentService.create(deliveryRequest);

            String msg;

            try {
                walletManagementService
                        .startRequestPayment(deliveryRequest);

                pendingPaymentService.markPaid(deliveryRequest);

                msg = "Your bid has just been accepted and account credited";

            } catch (Exception e) {
                msg = "Your bid has just been accepted. Awaiting Payment";
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

        } else {

            proposalService.updateCounterProposal(
                    request.getProposalId(),
                    request.getCounterProposal()
            );

            DeliveryRequest deliveryRequest =
                    deliveryRequestService
                            .getDeliveryRequestById(
                                    proposal.getDeliveryRequestId());

            notificationService.send(
                    AddAppNotificationDto.builder()
                            .message("Your bid was not accepted, please submit another bid. " +
                                    "Client proposes this amount " +
                                    proposal.getCounterProposal())
                            .title(NotificationTitle.DELIVERY)
                            .uid(proposal.getBusinessUserId())
                            .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                            .taskId(deliveryRequest.getTrackingId())
                            .build()
            );
        }

        DeliveryRequest deliveryRequest =
                deliveryRequestService.getDeliveryRequestById(
                        proposal.getDeliveryRequestId());

        return new DeliveryPriceProposalResponse(
                proposal,
                deliveryRequest,
                vehicle
        );

    }

    @Async
    public void sendNotificationToOtherProposals(UpdatePriceProposalRequest request,
                                                 Long userId){

        if (request.isAccept()) {

            SnapUser user = userService.getUserById(userId);

            DeliveryPriceProposal proposal =
                    proposalService.getProposal(
                            request.getProposalId(),
                            user
                    );

            DeliveryRequest deliveryRequest =
                    deliveryRequestService
                            .getDeliveryRequestById(
                                    proposal.getDeliveryRequestId());

            List<DeliveryPriceProposal> pendingProposals =
                    proposalService.getProposals(deliveryRequest);

            for (DeliveryPriceProposal p : pendingProposals) {

                notificationService.send(
                        AddAppNotificationDto.builder()
                                .message("Your bid was not successful, order is no longer available.")
                                .title(NotificationTitle.DELIVERY)
                                .uid(p.getBusinessUserId())
                                .build()
                );
            }
        }
    }

    public void processPayment(String trackId){
        DeliveryRequest request = deliveryRequestService.get(trackId);
        walletManagementService.startRequestPayment(request);
    }

    @Transactional
    public DeliveryPriceProposalResponse acceptProposal(Long userId,
                                                        String proposalId) {

        log.info("[ACCEPT_PROPOSAL] Start acceptProposal. userId={}, proposalId={}", userId, proposalId);

        SnapUser user = userService.getUserById(userId);

        log.info("[ACCEPT_PROPOSAL] Loaded user. id={}, walletKey={}",
                user.getId(), user.getWalletKey());

        DeliveryPriceProposal proposal =
                proposalService.getProposal(proposalId, user);

        log.info("[ACCEPT_PROPOSAL] Loaded proposal. id={}, vehicleId={}, requestId={}, fee(minor)={}, fee(naira)={}",
                proposal.getId(),
                proposal.getVehicleId(),
                proposal.getDeliveryRequestId(),
                proposal.getFee(),
                MoneyUtilities.fromMinorToDouble(proposal.getFee()));

        Vehicle vehicle =
                vehicleService.getVehicleById(proposal.getVehicleId());

        log.info("[ACCEPT_PROPOSAL] Loaded vehicle. id={}, available={}",
                vehicle.getId(),
                vehicle.getAvailable());

        boolean hasActiveRequest =
                deliveryRequestService.vehicleActiveRequest(vehicle);

        log.info("[ACCEPT_PROPOSAL] vehicleActiveRequest result. vehicleId={}, hasActiveRequest={}",
                vehicle.getId(),
                hasActiveRequest);

        // 1️⃣ Check vehicle availability
        if (hasActiveRequest) {
            log.warn("[ACCEPT_PROPOSAL] Vehicle already has an active request. vehicleId={}",
                    vehicle.getId());

            throw new FailedProcessException(
                    "Sorry, the vehicle is no longer available for your request"
            );
        }

        // ---------------- Wallet ----------------

        Wallet wallet =
                walletService.get(user.getWalletKey());

        log.info("[ACCEPT_PROPOSAL] Loaded wallet. walletKey={}, availableBalance(minor)={}, availableBalance(naira)={}, bookBalance(minor)={}",
                wallet.getWalletKey(),
                wallet.getAvailableBalance(),
                MoneyUtilities.fromMinorToDouble(wallet.getAvailableBalance()),
                wallet.getBookBalance());

        // ---------------- Delivery request ----------------

        DeliveryRequest request =
                deliveryRequestService
                        .getDeliveryRequestById(
                                proposal.getDeliveryRequestId());

        log.info("[ACCEPT_PROPOSAL] Loaded delivery request. id={}, trackingId={}, agreedFee(before)={}",
                request.getId(),
                request.getTrackingId(),
                request.getAgreedFee());

        request.setAgreedFee(proposal.getFee());
        deliveryRequestRepository.save(request);

        log.info("[ACCEPT_PROPOSAL] Updated agreed fee on request. requestId={}, agreedFee(minor)={}, agreedFee(naira)={}",
                request.getId(),
                request.getAgreedFee(),
                MoneyUtilities.fromMinorToDouble(request.getAgreedFee()));

        // ---------------- Wallet check ----------------

        Long balanceMinor = wallet.getAvailableBalance();
        Long feeMinor = proposal.getFee();

        log.info("[ACCEPT_PROPOSAL] Wallet check. walletKey={}, balance(minor)={}, balance(naira)={}, fee(minor)={}, fee(naira)={}",
                wallet.getWalletKey(),
                balanceMinor,
                MoneyUtilities.fromMinorToDouble(balanceMinor),
                feeMinor,
                MoneyUtilities.fromMinorToDouble(feeMinor));

        if (balanceMinor == null || feeMinor == null || balanceMinor < feeMinor) {

            log.warn("[ACCEPT_PROPOSAL] Insufficient balance. walletKey={}, balance(minor)={}, fee(minor)={}",
                    wallet.getWalletKey(),
                    balanceMinor,
                    feeMinor);

            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        // 3️⃣ Accept proposal
        proposalService.updateProposal(proposalId, true);

        log.info("[ACCEPT_PROPOSAL] Proposal accepted. proposalId={}", proposalId);

        // 4️⃣ Assign rider
        DeliveryRequest deliveryRequest =
                deliveryRequestService.assignToVehicleWithProposal(proposal);

        log.info("[ACCEPT_PROPOSAL] Rider assigned. requestId={}, vehicleId={}",
                deliveryRequest.getId(),
                vehicle.getId());

        // 5️⃣ Create pending payment
        pendingPaymentService.create(deliveryRequest);

        log.info("[ACCEPT_PROPOSAL] Pending payment created. requestId={}",
                deliveryRequest.getId());

        // 6️⃣ Take payment
        walletManagementService.startRequestPayment(deliveryRequest);

        log.info("[ACCEPT_PROPOSAL] Wallet debit completed. requestId={}, amount(minor)={}, amount(naira)={}",
                deliveryRequest.getId(),
                proposal.getFee(),
                MoneyUtilities.fromMinorToDouble(proposal.getFee()));

        pendingPaymentService.markPaid(deliveryRequest);

        log.info("[ACCEPT_PROPOSAL] Pending payment marked as paid. requestId={}",
                deliveryRequest.getId());

        // 7️⃣ Notify
        notificationService.send(
                AddAppNotificationDto.builder()
                        .message("Your bid has just been accepted and payment completed")
                        .title(NotificationTitle.DELIVERY)
                        .uid(deliveryRequest.getBusinessUserId())
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .taskId(deliveryRequest.getTrackingId())
                        .build()
        );

        log.info("[ACCEPT_PROPOSAL] Notification sent. userId={}, trackingId={}",
                deliveryRequest.getBusinessUserId(),
                deliveryRequest.getTrackingId());

        return new DeliveryPriceProposalResponse(
                proposal,
                deliveryRequest,
                vehicle
        );
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

        proposalService.updateProposal(proposalId, false);

        proposalService.updateCounterProposal(proposalId, counterProposal);

        DeliveryRequest deliveryRequest =
                deliveryRequestService
                        .getDeliveryRequestById(
                                proposal.getDeliveryRequestId());

        notificationService.send(
                AddAppNotificationDto.builder()
                        .message(
                                "Your bid was not accepted. Client proposes this amount " +
                                        counterProposal
                        )
                        .title(NotificationTitle.DELIVERY)
                        .uid(proposal.getBusinessUserId())
                        .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                        .taskId(deliveryRequest.getTrackingId())
                        .build()
        );
        Vehicle vehicle =
                vehicleService.getVehicleById(
                        proposal.getVehicleId());

        return new DeliveryPriceProposalResponse(
                proposal,
                deliveryRequest,
                vehicle
        );

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

//    public DeliveryPriceProposalResponse addPriceProposal(Long userId, CreatePriceProposalRequest request){
//        SnapUser user = userService.getUserById(userId);
//        Business business = businessService.getBusinessOfUser(user);
//        if(business==null){
//            throw new ResourceNotFoundException("There is no business owned to this user");
//        }
//        DeliveryRequest deliveryRequest = deliveryRequestService.get(request.getTrackId());
//        if(deliveryRequest.getStatus()!= DeliveryRequestStatus.NEW && deliveryRequest.getBusiness()!=null){
//            throw new FailedProcessException("Delivery request is no longer available");
//        }
//        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
//        if(!vehicle.getBusiness().getId().equals(business.getId())){
//            throw new FailedProcessException("Vehicle is not owned by this business");
//        }
//        if(!vehicle.getType().equals(deliveryRequest.getVehicleType())){
//            throw new FailedProcessException("Vehicle is not same as requested by customer");
//        }
//        DeliveryPriceProposal proposal = proposalService.createProposal(PriceProposalCreationDto.builder()
//                .amount(MoneyUtilities.fromDoubleToMinor(request.getProposedFee()))
//                .businessInitiated(true)
//                .comment(request.getComment())
//                .request(deliveryRequest)
//                .vehicle(vehicle)
//                .build());
//        notificationService.send(AddAppNotificationDto.builder()
//                .message("You have proposals for your delivery request")
//                .title(NotificationTitle.DELIVERY)
//                .uid(deliveryRequest.getUser().getIdentifier())
//                .task(NotificationTask.USER_DELIVERY.name())
//                .taskId(deliveryRequest.getTrackingId())
//                .build());
//        return new DeliveryPriceProposalResponse(proposal);
//    }

//    public DeliveryPriceProposalResponse updateProposal(Long userId, UpdatePriceProposalRequest request){
//        SnapUser user = userService.getUserById(userId);
//        DeliveryPriceProposal proposal = proposalService.getProposal(request.getProposalId(),user);
//        if(request.isAccept() && deliveryRequestService.vehicleActiveRequest(proposal.getVehicle())){
//            proposalService.updateProposal(request.getProposalId(), false);
//            throw new FailedProcessException("Sorry, the vehicle is no longer available for your request");
//        }
//        proposal = proposalService.updateProposal(request.getProposalId(),request.isAccept());
//        if(request.isAccept()) {
//            DeliveryRequest deliveryRequest = deliveryRequestService.assignToVehicleWithProposal(proposal);
//            pendingPaymentService.create(deliveryRequest);
//            String msg;
//            try {
//                walletManagementService.startRequestPayment(proposal.getRequest());
//                pendingPaymentService.markPaid(deliveryRequest);
//                msg = "Your bid has just been accepted and account credited";
//            }catch (Exception e){
//                msg = "Your bid has just been accepted. Awaiting Payment";
//            }
//            notificationService.send(AddAppNotificationDto.builder()
//                    .message(msg)
//                    .title(NotificationTitle.DELIVERY)
//                    .uid(deliveryRequest.getBusinessUserId())
//                    .task(NotificationTask.RIDER_DELIVERY.name())
//                    .taskId(deliveryRequest.getTrackingId())
//                    .build());
//        }
//        else{
//            proposalService.updateCounterProposal(request.getProposalId(), request.getCounterProposal());
//            notificationService.send(AddAppNotificationDto.builder()
//                    .message("Your bid was not accepted, please submit another bid. " +
//                            "Client proposes this amount " + proposal.getCounterProposal())
//                    .title(NotificationTitle.DELIVERY)
//                    .uid(proposal.getBusinessUserId())
//                            .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
//                            .taskId(proposal.getRequest().getTrackingId())
//                    .build());
//        }
//        return new DeliveryPriceProposalResponse(proposal);
//    }

//    @Async
//    public void sendNotificationToOtherProposals(UpdatePriceProposalRequest request, Long userId){
//        if(request.isAccept()){
//            SnapUser user = userService.getUserById(userId);
//            DeliveryPriceProposal proposal = proposalService.getProposal(request.getProposalId(),user);
//            DeliveryRequest deliveryRequest = proposal.getRequest();
//            List<DeliveryPriceProposal> pendingProposals = proposalService.getProposals(deliveryRequest);
//            for(DeliveryPriceProposal p : pendingProposals){
//                notificationService.send(AddAppNotificationDto.builder()
//                        .message("Your bid was not successful, order is no longer available.")
//                        .title(NotificationTitle.DELIVERY)
//                        .uid(p.getBusinessUserId())
//                        .build());
//            }
//        }
//    }

//    @Transactional
//    public DeliveryPriceProposalResponse acceptProposal(Long userId, String proposalId) {
//
//        SnapUser user = userService.getUserById(userId);
//        DeliveryPriceProposal proposal =
//                proposalService.getProposal(proposalId, user);
//
//        // 1️⃣ Check vehicle availability
//        if (deliveryRequestService.vehicleActiveRequest(proposal.getVehicle())) {
//            throw new FailedProcessException(
//                    "Sorry, the vehicle is no longer available for your request"
//            );
//        }
//
//        DeliveryRequest request = proposal.getRequest();
//        request.setAgreedFee(proposal.getFee());
//        deliveryRequestRepository.save(request);
//
//        // 2️⃣ 🔥 CHECK WALLET BALANCE FIRST (NO DEBIT)
//        if (user.getBalance().compareTo(proposal.getFee()) < 0) {
//            throw new InsufficientBalanceException("Insufficient wallet balance");
//        }
//
//        // 3️⃣ Accept proposal
//        proposalService.updateProposal(proposalId, true);
//
//        // 4️⃣ Assign rider ONLY after wallet check
//        DeliveryRequest deliveryRequest =
//                deliveryRequestService.assignToVehicleWithProposal(proposal);
//
//        // 5️⃣ Create pending payment
//        pendingPaymentService.create(deliveryRequest);
//
//        // 6️⃣ Take payment
//        walletManagementService.startRequestPayment(deliveryRequest);
//
//        pendingPaymentService.markPaid(deliveryRequest);
//
//        // 7️⃣ Notify
//        notificationService.send(AddAppNotificationDto.builder()
//                .message("Your bid has just been accepted and payment completed")
//                .title(NotificationTitle.DELIVERY)
//                .uid(deliveryRequest.getBusinessUserId())
//                .task(NotificationTask.RIDER_DELIVERY.name())
//                .taskId(deliveryRequest.getTrackingId())
//                .build());
//
//        return new DeliveryPriceProposalResponse(proposal);
//    }

//    @Transactional
//    public DeliveryPriceProposalResponse rejectProposal(
//            Long userId,
//            String proposalId,
//            Long counterProposal
//    ) {
//
//        SnapUser user = userService.getUserById(userId);
//        DeliveryPriceProposal proposal =
//                proposalService.getProposal(proposalId, user);
//
//        // Reject proposal
//        proposalService.updateProposal(proposalId, false);
//
//        // Set counter proposal
//        proposalService.updateCounterProposal(proposalId, counterProposal);
//
//        // Notify rider
//        notificationService.send(AddAppNotificationDto.builder()
//                .message(
//                        "Your bid was not accepted. Client proposes this amount " +
//                                counterProposal
//                )
//                .title(NotificationTitle.DELIVERY)
//                .uid(proposal.getBusinessUserId())
//                .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
//                .taskId(proposal.getRequest().getTrackingId())
//                .build());
//
//        return new DeliveryPriceProposalResponse(proposal);
//    }

}
