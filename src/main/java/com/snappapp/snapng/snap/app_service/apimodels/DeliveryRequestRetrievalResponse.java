package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryFrequency;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.SendType;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequestRetrievalResponse {
    @JsonIgnore
    private DeliveryRequest request;

    @JsonIgnore
    private LocalDateTime expiryTimeForPayment;

    @JsonIgnore
    private String proposalId;

    @JsonProperty("trackId")
    public String trackId(){
        return request.getTrackingId();
    }

    @JsonProperty("recipient")
    public String recipient(){
        return request.getRecipientName();
    }

    @JsonProperty("recipientPhone")
    public String recipientPhone(){
        return request.getRecipientNumber();
    }

    @JsonProperty("note")
    public String note(){
        return request.getAdditionalNote();
    }

    @JsonProperty("description")
    public String description(){
        return request.getDescription();
    }

    @JsonProperty("worth")
    public Double worth(){
        return MoneyUtilities.fromMinorToDouble(request.getWorth());
    }

    @JsonProperty("sendType")
    public SendType sendType(){
        return request.getSendType();
    }

    @JsonProperty("vehicle")
    public VehicleType vehicleType(){
        return request.getVehicleType();
    }

    @JsonProperty("frequency")
    public DeliveryFrequency deliveryFrequency(){
        return request.getDeliveryFrequency();
    }

    @JsonProperty("status")
    public DeliveryRequestStatus status(){
        return request.getStatus();
    }

    @JsonProperty("pickup")
    public AddressRetrievalResponse pickup(){
        return AddressRetrievalResponse.builder().location(request.getPickupLocation()).build();
    }

    @JsonProperty("destination")
    public AddressRetrievalResponse destination(){
        return AddressRetrievalResponse.builder().location(request.getDestinationLocation()).build();
    }

    @JsonProperty("suggestedFee")
    public Double getSuggestedFee(){
        return MoneyUtilities.fromMinorToDouble(request.getCalculatedFee());
    }

    @JsonProperty("startTime")
    public LocalDateTime getStartTime(){
        return request.getStartTime();
    }

    @JsonProperty("endTime")
    public LocalDateTime getEndTime(){
        return request.getEndTime();
    }

    @JsonProperty("agreedFee")
    public Double getFee(){
        return MoneyUtilities.fromMinorToDouble(request.getAgreedFee());
    }

    @JsonProperty("remainderSecondsForPayment")
    public Long getPaymentTimerLeftInSeconds(){
        if(expiryTimeForPayment!=null){
            return Duration.between(LocalDateTime.now(),expiryTimeForPayment).getSeconds();
        }
        return 0L;
    }

    @JsonProperty("weight")
    public String getWeight(){
        return request.getWeight();
    }
}
