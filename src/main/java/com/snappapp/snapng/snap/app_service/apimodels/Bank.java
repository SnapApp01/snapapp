package com.snappapp.snapng.snap.app_service.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Bank {
    private String name;
    private String code;
}
