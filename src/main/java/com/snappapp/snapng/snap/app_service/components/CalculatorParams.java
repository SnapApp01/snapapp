package com.snappapp.snapng.snap.app_service.components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CalculatorParams {
    private double vat;
    private double processorCharges;
    private long dieselPrice;
    private long fuelPrice;
    private long minCap;
    private double distanceInKm;
}
