package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.TripOfferStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "planned_trip_offers")
@Data
public class PlannedTripOffer extends BaseEntity {
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",referencedColumnName = "id",nullable = false)
    private SnapUser user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id",referencedColumnName = "id",nullable = false)
    private PlannedTrip trip;
    private Long userProposedFee;
    private Long businessProposedFee;
    @Column(unique = true)
    private String reference;
    @Enumerated(EnumType.STRING)
    private TripOfferStatus status;
    private String bookingId;
    private String additionalNote;
}
