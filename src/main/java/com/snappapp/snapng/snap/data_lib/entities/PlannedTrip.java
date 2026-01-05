package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "planned_trips")
@Data
public class PlannedTrip extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_id",referencedColumnName = "id")
    private Business business;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "start_location_id",referencedColumnName = "id")
    private Location start;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "end_location_id",referencedColumnName = "id")
    private Location end;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id",referencedColumnName = "id")
    private Vehicle vehicle;
    @Column(unique = true)
    private String reference;
    @Enumerated(EnumType.STRING)
    private PlannedTripStatus status;
    private LocalDate tripDate;

    @PostLoad
    public void onLoad(){
        if(PlannedTripStatus.OPEN.equals(this.getStatus())){
            if(LocalDate.now().isAfter(this.tripDate)){
                this.setStatus(PlannedTripStatus.CLOSED);
            }
        }
    }
}
