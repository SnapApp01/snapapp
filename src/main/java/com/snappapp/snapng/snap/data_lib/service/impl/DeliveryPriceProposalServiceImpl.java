package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.PriceProposalCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryPriceProposalRepository;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestRepository;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryPriceProposalService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestService;
import com.snappapp.snapng.snap.data_lib.service.VehicleService;
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
    private final VehicleService vehicleService;
    private final BusinessService businessService;
    private final DeliveryRequestRepository deliveryRequestRepository;


    public DeliveryPriceProposalServiceImpl(DeliveryPriceProposalRepository repo, VehicleService vehicleService, BusinessService businessService, DeliveryRequestRepository deliveryRequestRepository) {
        this.repo = repo;
        this.vehicleService = vehicleService;
        this.businessService = businessService;
        this.deliveryRequestRepository = deliveryRequestRepository;
    }

    @Override
    public DeliveryPriceProposal createProposal(PriceProposalCreationDto creationDto) {


        Optional<DeliveryPriceProposal> optional =
                repo.findByVehicleIdAndDeliveryRequestIdAndStatusAndActiveTrue(
                        creationDto.getVehicleId(),
                        creationDto.getRequestId(),
                        FeeProposalStatus.PENDING
                );

        if (optional.isPresent()
                && optional.get().getBusinessInitiated() == creationDto.isBusinessInitiated()) {

            DeliveryPriceProposal existing = optional.get();
            existing.setComment(creationDto.getComment().trim());
            existing.setFee(creationDto.getAmount());
            return repo.save(existing);
        }

        if (optional.isPresent()) {
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

        proposal.setDeliveryRequestId(creationDto.getRequestId());
        proposal.setVehicleId(creationDto.getVehicleId());

        // resolve business through vehicle -> businessId
        Vehicle vehicle = vehicleService.getVehicleById(creationDto.getVehicleId());

        Business business =
                businessService.getBusinessById(vehicle.getBusinessId());

        business.getUsers();

        proposal.setBusinessUserId(
                business.getUsers()
                        .iterator()
                        .next()
                        .getIdentifier()
        );

        return repo.save(proposal);
    }

    @Override
    public DeliveryPriceProposal updateProposal(String proposalId, boolean accepted) {
        DeliveryPriceProposal proposal = repo.findByProposalIdAndActiveTrue(proposalId)
                .orElseThrow(()->new ResourceNotFoundException("Proposal not found"));
        proposal.setStatus(accepted ? FeeProposalStatus.ACCEPTED : FeeProposalStatus.REJECTED);
        return repo.save(proposal);
    }

    @Override
    public void updateCounterProposal(String proposalId, long counterProposal) {
        DeliveryPriceProposal proposal = repo.findByProposalIdAndActiveTrue(proposalId)
                .orElseThrow(()->new ResourceNotFoundException("Proposal not found"));
        proposal.setCounterProposal(counterProposal);
        repo.save(proposal);
    }

    @Override
    public List<DeliveryPriceProposal> getProposals(Vehicle vehicle) {

        return repo.findByVehicleIdAndActiveTrueAndStatusAndBusinessInitiatedFalse(
                vehicle.getId(),
                FeeProposalStatus.PENDING
        );
    }

    @Override
    public DeliveryPriceProposal getProposal(String proposalId, SnapUser user) {

        DeliveryPriceProposal proposal =
                repo.findByProposalIdAndActiveTrue(proposalId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Proposal id not found"));

        DeliveryRequest request =
                deliveryRequestRepository.findById(proposal.getDeliveryRequestId())
                        .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (!request.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Proposal id not found for user");
        }

        return proposal;
    }

    @Override
    public DeliveryPriceProposal getProposal(DeliveryRequest request, Business business) {

        List<DeliveryPriceProposal> proposals =
                repo.findByDeliveryRequestId(request.getId());

        for (DeliveryPriceProposal proposal : proposals) {

            Vehicle vehicle =
                    vehicleService.getVehicleById(proposal.getVehicleId());

            if (vehicle.getBusinessId().equals(business.getId())) {
                return proposal;
            }
        }

        throw new ResourceNotFoundException("Proposal id not found for business");
    }

    @Override
    public List<DeliveryPriceProposal> getProposals(DeliveryRequest request) {

        return repo.findByDeliveryRequestIdAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(
                request.getId(),
                FeeProposalStatus.PENDING
        );
    }


//    @Override
//    public DeliveryPriceProposal createProposal(PriceProposalCreationDto creationDto) {
//        Optional<DeliveryPriceProposal> optional = repo
//                .findByVehicleAndRequestAndStatusAndActiveTrue(creationDto.getVehicle(),creationDto.getRequest(), FeeProposalStatus.PENDING);
//        if(optional.isPresent() && optional.get().getBusinessInitiated()==creationDto.isBusinessInitiated()){
//            DeliveryPriceProposal existing = optional.get();
//            existing.setComment(creationDto.getComment().trim());
//            existing.setFee(creationDto.getAmount());
//            return repo.save(existing);
//        }
//        if(optional.isPresent()){
//            DeliveryPriceProposal existing = optional.get();
//            existing.setStatus(FeeProposalStatus.REJECTED);
//            repo.save(existing);
//        }
//        DeliveryPriceProposal proposal = new DeliveryPriceProposal();
//        proposal.setStatus(FeeProposalStatus.PENDING);
//        proposal.setFee(creationDto.getAmount());
//        proposal.setComment(creationDto.getComment().trim());
//        proposal.setBusinessInitiated(creationDto.isBusinessInitiated());
//        proposal.setProposalId(IdUtilities.useDateTimeAtomic());
//        proposal.setDeliveryRequestId(creationDto.getRequest().getId());
//        proposal.setVehicleId(creationDto.getVehicle().getId());
//        Business business = proposal.getVehicle().getBusiness();
//        business.getUsers();
//        proposal.setBusinessUserId(business.getUsers().iterator().next().getIdentifier());
//        return repo.save(proposal);
//    }

//    @Override
//    public List<DeliveryPriceProposal> getProposals(Vehicle vehicle) {
//        return repo.findByVehicleAndActiveTrueAndStatusAndBusinessInitiatedFalse(vehicle,FeeProposalStatus.PENDING);
//    }
//
//    @Override
//    public DeliveryPriceProposal getProposal(String proposalId, SnapUser user) {
//        return repo.findByProposalIdAndRequest_UserAndActiveTrue(proposalId, user).orElseThrow(()->new ResourceNotFoundException("Proposal id not found for user"));
//    }
//
//    @Override
//    public DeliveryPriceProposal getProposal(DeliveryRequest request, Business business) {
//        return repo.findByRequestAndVehicle_BusinessAndActiveTrue(request, business).orElseThrow(()->new ResourceNotFoundException("Proposal id not found for business"));
//    }

    //    @Override
//    public List<DeliveryPriceProposal> getProposals(DeliveryRequest request) {
//        return repo.findByRequestAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(request,FeeProposalStatus.PENDING);
//    }
}
