package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class VehicleRetrievalResponse {
    private Vehicle vehicle;
    @JsonProperty("id")
    public String id(){
        return vehicle.getVehicleId();
    }

    @JsonProperty("plateNumber")
    public String plate(){
        return vehicle.getPlateNumber();
    }

    @JsonProperty("description")
    public String description(){
        return vehicle.getDescription();
    }

    @JsonProperty("year")
    public String year(){
        return vehicle.getYear()+"";
    }

    @JsonProperty("type")
    public VehicleType type(){
        return vehicle.getType();
    }

    @JsonProperty("available")
    public boolean available(){
        return vehicle.getAvailable();
    }
}
