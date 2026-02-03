package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRetrievalResponse {

    @JsonIgnore
    private Location location;

    @JsonProperty("address")
    public String address(){
        return location.getAddress();
    }

    @JsonProperty("landmark")
    public String landMark(){
        return location.getLandmark();
    }

    @JsonProperty("state")
    public String state(){
        return location.getState();
    }

    @JsonProperty("city")
    public String city(){
        return location.getCity();
    }

    @JsonProperty("longitude")
    public Double longitude(){
        return location.getLongitude();
    }

    @JsonProperty("latitude")
    public Double latitude(){
        return location.getLatitude();
    }
}
