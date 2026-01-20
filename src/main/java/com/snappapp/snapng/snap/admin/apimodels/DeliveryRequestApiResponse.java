package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

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
}
