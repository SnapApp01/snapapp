package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTripOffer;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class TripOfferResponse {
    @JsonIgnore
    private PlannedTripOffer tripOffer;

    public TripOfferResponse(PlannedTripOffer offer){
        this.tripOffer = offer;
    }

    @JsonProperty("description")
    public String getDescription(){
        return tripOffer.getDescription();
    }

    @JsonProperty("worth")
    public Double getWorth(){
        return MoneyUtilities.fromMinorToDouble(tripOffer.getWorth());
    }

    @JsonProperty("weight")
    public String getWeight(){
        return tripOffer.getWeight();
    }

    @JsonProperty("pickup")
    public AddressRetrievalResponse pickup(){
        return AddressRetrievalResponse.builder().location(tripOffer.getPickupLocation()).build();
    }

    @JsonProperty("destination")
    public AddressRetrievalResponse destination(){
        return AddressRetrievalResponse.builder().location(tripOffer.getDestinationLocation()).build();
    }

    @JsonProperty("recipient")
    public String recipient(){
        return tripOffer.getRecipientName();
    }

    @JsonProperty("recipientPhone")
    public String recipientPhone(){
        return tripOffer.getRecipientNumber();
    }

    @JsonProperty("userOffer")
    public Double getUserOffer(){
        return MoneyUtilities.fromMinorToDouble(tripOffer.getUserProposedFee());
    }

    @JsonProperty("riderOffer")
    public Double getRiderOffer(){
        return MoneyUtilities.fromMinorToDouble(tripOffer.getBusinessProposedFee());
    }

    @JsonProperty("reference")
    public String getReference(){
        return tripOffer.getReference();
    }

    @JsonProperty("status")
    public String getStatus(){
        return tripOffer.getStatus().name();
    }

    @JsonProperty("time")
    public LocalDateTime getTime(){
        return tripOffer.getCreatedAt();
    }

    @JsonProperty("note")
    public String getNote(){
        return tripOffer.getAdditionalNote();
    }

    @JsonProperty("trackId")
    public String getRequestTrackId(){
        return tripOffer.getBookingId();
    }
}
