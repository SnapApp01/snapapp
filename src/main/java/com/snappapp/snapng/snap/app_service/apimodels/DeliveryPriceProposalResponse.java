package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryPriceProposal;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPriceProposalResponse {
    private DeliveryPriceProposal proposal;

    @JsonProperty("id")
    public String getId(){
        return proposal.getProposalId();
    }

    @JsonProperty("comment")
    public String getComment(){
        return proposal.getComment();
    }

    @JsonProperty("fee")
    public double getFee(){
        return MoneyUtilities.fromMinorToDouble(proposal.getFee());
    }

    @JsonProperty("status")
    public FeeProposalStatus getStatus(){
        return proposal.getStatus();
    }

    @JsonProperty("trackingId")
    public String getRequestId(){
        return proposal.getRequest().getTrackingId();
    }

    @JsonProperty("vehicle")
    public String getVehicle(){
        return  proposal.getVehicle().getDescription();
    }

    @JsonProperty("vehicleYear")
    public String getYear(){
        return proposal.getVehicle().getYear()+"";
    }

    @JsonProperty("plate")
    public String getPlate(){
        return proposal.getVehicle().getPlateNumber();
    }
}
