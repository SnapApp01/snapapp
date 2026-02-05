package com.snappapp.snapng.snap.admin.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequestApiResponse {

    private String id;
    private String startAddress;
    private String endAddress;
    private String currentPosition;
    private String customerName;
    private String customerPhone;
    private String status;
    private String recipientName;
    private String recipientPhone;
    private Long worthOfGood;
    private Long fee;
    private String description;
    private String vehicleType;
    private String vehiclePlate;
    private String vehicleId;
    private Integer frequency;

    public DeliveryRequestApiResponse(DeliveryRequest request) {

        this.id = request.getTrackingId();

        if (request.getPickupLocation() != null) {
            this.startAddress = request.getPickupLocation().getAddress();
        }

        if (request.getDestinationLocation() != null) {
            this.endAddress = request.getDestinationLocation().getAddress();
        }

        // You do NOT have current location in DeliveryRequest entity
        this.currentPosition = null;

        if (request.getUser() != null) {
            this.customerName = request.getUser().getFullName();
            this.customerPhone = request.getUser().getPhoneNumber();
        }

        this.status = request.getStatus() != null
                ? request.getStatus().name()
                : null;

        this.recipientName = request.getRecipientName();
        this.recipientPhone = request.getRecipientNumber();

        this.worthOfGood = request.getWorth();
        this.fee = request.getAgreedFee();

        this.description = request.getDescription();

        if (request.getVehicle() != null) {
            this.vehicleId = String.valueOf(request.getVehicle().getId());
            this.vehiclePlate = request.getVehicle().getPlateNumber();
        }

        if (request.getVehicleType() != null) {
            this.vehicleType = request.getVehicleType().name();
        }

        if (request.getDeliveryFrequency() != null) {
            this.frequency = request.getDeliveryFrequency().ordinal();
        }
    }
}
