package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAddressRequest {
    private String address;
    private String landMark;
    private String state;
    private String city;
    private double longitude;
    private double latitude;
}
