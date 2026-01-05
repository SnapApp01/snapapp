package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
public class Location extends BaseEntity {
    private String address;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;
    private String description;
    private String landmark;
}
