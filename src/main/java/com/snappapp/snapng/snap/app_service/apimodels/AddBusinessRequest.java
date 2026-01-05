package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AddBusinessRequest {
    private String companyName;
}
