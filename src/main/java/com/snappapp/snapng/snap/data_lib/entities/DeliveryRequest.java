package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryFrequency;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.SendType;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_requests")
@Getter
@Setter
public class DeliveryRequest extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private SendType sendType;
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    @Enumerated(EnumType.STRING)
    private DeliveryFrequency deliveryFrequency;
    @Enumerated(EnumType.STRING)
    private DeliveryRequestStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Long worth;
    private String weight;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pickup_location_id", referencedColumnName = "id")
    private Location pickupLocation;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_location_id", referencedColumnName = "id")
    private Location destinationLocation;
    private String recipientName;
    private String recipientNumber;
    private String additionalNote;
    @Column(unique = true,nullable = false)
    private String trackingId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",referencedColumnName = "id",nullable = false)
    private SnapUser user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_id",referencedColumnName = "id")
    private Business business;
    private String businessUserId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id",referencedColumnName = "id")
    private Vehicle vehicle;
    private Long calculatedFee;
    private Long agreedFee;
    @OneToMany(
            mappedBy = "request",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<DeliveryPriceProposal> proposals = new ArrayList<>();
}
