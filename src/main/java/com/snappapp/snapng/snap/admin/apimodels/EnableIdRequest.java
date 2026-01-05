package com.snappapp.snapng.snap.admin.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnableIdRequest extends IdRequest{
    private boolean enabled;
}
