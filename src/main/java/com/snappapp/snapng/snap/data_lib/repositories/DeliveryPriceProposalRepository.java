package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPriceProposalRepository extends JpaRepository<DeliveryPriceProposal,Long> {
    Optional<DeliveryPriceProposal> findByProposalIdAndActiveTrue(String proposalId);
    Optional<DeliveryPriceProposal> findByProposalIdAndRequest_UserAndActiveTrue(String proposalId, SnapUser user);
    Optional<DeliveryPriceProposal> findByProposalIdAndVehicle_BusinessAndActiveTrue(String proposalId, Business business);
    Optional<DeliveryPriceProposal> findByRequestAndVehicle_BusinessAndActiveTrue(DeliveryRequest request, Business business);
    Optional<DeliveryPriceProposal> findByVehicleAndRequestAndStatusAndActiveTrue(Vehicle vehicle, DeliveryRequest request, FeeProposalStatus status);
    List<DeliveryPriceProposal> findByVehicleAndActiveTrueAndStatusAndBusinessInitiatedFalse(Vehicle vehicle, FeeProposalStatus status);
    List<DeliveryPriceProposal> findByRequestAndActiveTrueAndStatusAndBusinessInitiatedTrueOrderByFeeAsc(DeliveryRequest request,FeeProposalStatus status);
}
