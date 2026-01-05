package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class Vehicle extends BaseEntity {
    private String plateNumber;
    @Column(unique = true)
    private String vehicleId;
    private Integer year;
    @Enumerated(EnumType.STRING)
    private VehicleType type;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_id",referencedColumnName = "id")
    private Business business;
    private Boolean available;
}
