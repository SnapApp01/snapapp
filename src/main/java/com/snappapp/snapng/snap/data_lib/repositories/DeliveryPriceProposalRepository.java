package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPriceProposalRepository
        extends JpaRepository<DeliveryPriceProposal, Long> {

    Optional<DeliveryPriceProposal> findByProposalIdAndActiveTrue(String proposalId);

    Optional<DeliveryPriceProposal>
    findByVehicleIdAndDeliveryRequestIdAndStatusAndActiveTrue(
            Long vehicleId,
            Long deliveryRequestId,
            FeeProposalStatus status
    );

    List<DeliveryPriceProposal>
    findByDeliveryRequestIdAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(
            Long deliveryRequestId,
            FeeProposalStatus status
    );

    List<DeliveryPriceProposal>
    findByVehicleIdAndActiveTrueAndStatusAndBusinessInitiatedFalse(
            Long vehicleId,
            FeeProposalStatus status
    );

    List<DeliveryPriceProposal>
    findByDeliveryRequestId(Long deliveryRequestId);

    Optional<DeliveryPriceProposal>
    findTopByDeliveryRequestIdOrderByCreatedAtDesc(Long deliveryRequestId);

//    Optional<DeliveryPriceProposal> findByProposalIdAndActiveTrue(String proposalId);
//
//    Optional<DeliveryPriceProposal> findByProposalIdAndRequest_UserAndActiveTrue(
//            String proposalId,
//            SnapUser user
//    );
//
//    Optional<DeliveryPriceProposal> findByVehicleIdAndDeliveryRequestIdAndStatusAndActiveTrue(Long vehicleId, Long requestId, FeeProposalStatus feeProposalStatus);
//
//    Optional<DeliveryPriceProposal> findByRequestAndVehicle_BusinessAndActiveTrue(
//            DeliveryRequest request,
//            Business business
//    );
//
//    List<DeliveryPriceProposal> findByVehicleAndActiveTrueAndStatusAndBusinessInitiatedFalse(
//            Vehicle vehicle,
//            FeeProposalStatus status
//    );
//
//    List<DeliveryPriceProposal> findByRequestAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(
//            DeliveryRequest request,
//            FeeProposalStatus status
//    );




//    Optional<DeliveryPriceProposal> findByVehicleAndRequestAndStatusAndActiveTrue(
//            Vehicle vehicle,
//            DeliveryRequest request,
//            FeeProposalStatus status
//    );
//
//
//    Optional<DeliveryPriceProposal> findByProposalIdAndVehicle_BusinessAndActiveTrue(
//            String proposalId,
//            Business business
//    );
//    List<DeliveryPriceProposal> findByRequest(DeliveryRequest request);
//
//    // Optional: get proposals by request and status
//    List<DeliveryPriceProposal> findByRequestAndStatus(DeliveryRequest request, FeeProposalStatus status);
//
//    // Optional: get a single proposal by request, e.g., latest created
//    Optional<DeliveryPriceProposal> findTopByRequestOrderByCreatedAtDesc(DeliveryRequest request);

}

