package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.dtos.PriceProposalCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTask;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryPriceProposalRepository;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryPriceProposalService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeliveryPriceProposalServiceImpl implements DeliveryPriceProposalService {

    private final DeliveryPriceProposalRepository repo;
    private final AppNotificationService appNotificationService;

    public DeliveryPriceProposalServiceImpl(DeliveryPriceProposalRepository repo, AppNotificationService appNotificationService) {
        this.repo = repo;
        this.appNotificationService = appNotificationService;
    }

    @Override
    public DeliveryPriceProposal createProposal(PriceProposalCreationDto creationDto) {
        Optional<DeliveryPriceProposal> optional = repo
                .findByVehicleAndRequestAndStatusAndActiveTrue(creationDto.getVehicle(),creationDto.getRequest(), FeeProposalStatus.PENDING);
        if(optional.isPresent() && optional.get().getBusinessInitiated()==creationDto.isBusinessInitiated()){
            DeliveryPriceProposal existing = optional.get();
            existing.setComment(creationDto.getComment().trim());
            existing.setFee(creationDto.getAmount());
            return repo.save(existing);
        }
        if(optional.isPresent()){
            DeliveryPriceProposal existing = optional.get();
            existing.setStatus(FeeProposalStatus.REJECTED);
            repo.save(existing);
        }
        DeliveryPriceProposal proposal = new DeliveryPriceProposal();
        proposal.setStatus(FeeProposalStatus.PENDING);
        proposal.setFee(creationDto.getAmount());
        proposal.setComment(creationDto.getComment().trim());
        proposal.setBusinessInitiated(creationDto.isBusinessInitiated());
        proposal.setProposalId(IdUtilities.useDateTimeAtomic());
        proposal.setRequest(creationDto.getRequest());
        proposal.setVehicle(creationDto.getVehicle());
        Business business = proposal.getVehicle().getBusiness();
        business.getUsers();
        proposal.setBusinessUserId(business.getUsers().iterator().next().getIdentifier());
        return repo.save(proposal);
    }

    @Override
    public List<DeliveryPriceProposal> getProposals(Vehicle vehicle) {
        return repo.findByVehicleAndActiveTrueAndStatusAndBusinessInitiatedFalse(vehicle,FeeProposalStatus.PENDING);
    }

    @Override
    public DeliveryPriceProposal getProposal(String proposalId, SnapUser user) {
        return repo.findByProposalIdAndRequest_UserAndActiveTrue(proposalId, user).orElseThrow(()->new ResourceNotFoundException("Proposal id not found for user"));
    }

    @Override
    public DeliveryPriceProposal getProposal(DeliveryRequest request, Business business) {
        return repo.findByRequestAndVehicle_BusinessAndActiveTrue(request, business).orElseThrow(()->new ResourceNotFoundException("Proposal id not found for business"));
    }
    @Override
    public DeliveryPriceProposal updateProposal(String proposalId, boolean accepted) {
        DeliveryPriceProposal proposal = repo.findByProposalIdAndActiveTrue(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        proposal.setStatus(accepted ? FeeProposalStatus.ACCEPTED : FeeProposalStatus.REJECTED);

        DeliveryPriceProposal saved = repo.save(proposal);

        if (!accepted) {
            appNotificationService.save(
                    AddAppNotificationDto.builder()
                            .message(
                                    "Your bid was not accepted, please submit another bid. " +
                                            "Client proposes this amount " + proposal.getCounterProposal()
                            )
                            .title(NotificationTitle.DELIVERY)
                            .uid(proposal.getBusinessUserId())
                            .task(NotificationTask.RIDER_DELIVERY_PROPOSAL.name())
                            .taskId(proposal.getRequest().getTrackingId())
                            .build()
            );
        }

        return saved;
    }

//    @Override
//    public DeliveryPriceProposal updateProposal(String proposalId, boolean accepted) {
//        DeliveryPriceProposal proposal = repo.findByProposalIdAndActiveTrue(proposalId)
//                .orElseThrow(()->new ResourceNotFoundException("Proposal not found"));
//        proposal.setStatus(accepted ? FeeProposalStatus.ACCEPTED : FeeProposalStatus.REJECTED);
//        return repo.save(proposal);
//    }

    @Override
    public List<DeliveryPriceProposal> getProposals(DeliveryRequest request) {
        return repo.findByRequestAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(request,FeeProposalStatus.PENDING);
    }

    @Override
    public void updateCounterProposal(String proposalId, long counterProposal) {
        DeliveryPriceProposal proposal = repo.findByProposalIdAndActiveTrue(proposalId)
                .orElseThrow(()->new ResourceNotFoundException("Proposal not found"));
        proposal.setCounterProposal(counterProposal);
        repo.save(proposal);
    }
}
