package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryPriceProposal;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryPriceProposalResponse {

    @JsonIgnore
    private DeliveryPriceProposal proposal;

    @JsonIgnore
    private DeliveryRequest deliveryRequest;

    @JsonIgnore
    private Vehicle vehicle;

    /**
     * This constructor is the only place where we resolve entities
     */
    public DeliveryPriceProposalResponse(
            DeliveryPriceProposal proposal,
            DeliveryRequest deliveryRequest,
            Vehicle vehicle
    ) {
        this.proposal = proposal;
        this.deliveryRequest = deliveryRequest;
        this.vehicle = vehicle;
    }

    @JsonProperty("id")
    public String getId() {
        return proposal.getProposalId();
    }

    @JsonProperty("comment")
    public String getComment() {
        return proposal.getComment();
    }

    @JsonProperty("fee")
    public double getFee() {
        return MoneyUtilities.fromMinorToDouble(proposal.getFee());
    }

    @JsonProperty("status")
    public FeeProposalStatus getStatus() {
        return proposal.getStatus();
    }

    @JsonProperty("trackingId")
    public String getRequestId() {
        return deliveryRequest.getTrackingId();
    }

    @JsonProperty("vehicle")
    public String getVehicle() {
        return vehicle.getDescription();
    }

    @JsonProperty("vehicleYear")
    public String getYear() {
        return String.valueOf(vehicle.getYear());
    }

    @JsonProperty("plate")
    public String getPlate() {
        return vehicle.getPlateNumber();
    }

    @JsonProperty("counterProposal")
    public Long getCounterProposal() {
        return proposal.getCounterProposal();
    }
}

//package com.snappapp.snapng.snap.app_service.apimodels;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.snappapp.snapng.snap.data_lib.entities.DeliveryPriceProposal;
//import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
//import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class DeliveryPriceProposalResponse {
//    private DeliveryPriceProposal proposal;
//
//    @JsonProperty("id")
//    public String getId(){
//        return proposal.getProposalId();
//    }
//
//    @JsonProperty("comment")
//    public String getComment(){
//        return proposal.getComment();
//    }
//
//    @JsonProperty("fee")
//    public double getFee(){
//        return MoneyUtilities.fromMinorToDouble(proposal.getFee());
//    }
//
//    @JsonProperty("status")
//    public FeeProposalStatus getStatus(){
//        return proposal.getStatus();
//    }
//
//    @JsonProperty("trackingId")
//    public String getRequestId(){
//        return proposal.getRequest().getTrackingId();
//    }
//
//    @JsonProperty("vehicle")
//    public String getVehicle(){
//        return  proposal.getVehicle().getDescription();
//    }
//
//    @JsonProperty("vehicleYear")
//    public String getYear(){
//        return proposal.getVehicle().getYear()+"";
//    }
//
//    @JsonProperty("plate")
//    public String getPlate(){
//        return proposal.getVehicle().getPlateNumber();
//    }
//
//    @JsonProperty("counterProposal")
//    public Long getCounterProposal(){ return proposal.getCounterProposal();}
//}
