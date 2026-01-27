package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.PriceProposalCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.*;

import java.util.List;

public interface DeliveryPriceProposalService {
    DeliveryPriceProposal createProposal(PriceProposalCreationDto creationDto);
    List<DeliveryPriceProposal> getProposals(Vehicle vehicle);
    DeliveryPriceProposal getProposal(String proposalId, SnapUser user);
    DeliveryPriceProposal getProposal(DeliveryRequest request, Business business);
    DeliveryPriceProposal updateProposal(String proposalId, boolean accepted);
    List<DeliveryPriceProposal> getProposals(DeliveryRequest request);

    void updateCounterProposal(String proposalId, long counterProposal);
}
