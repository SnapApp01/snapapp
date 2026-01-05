package com.snappapp.snapng.snap.app_service.components;

public class BicycleMinimumCostCalculator extends MinimumCostCalculator {
    public BicycleMinimumCostCalculator(CalculatorParams params) {
        super(params);
    }

    @Override
    public long calculate() {
        long val =  (long)(getParams().getDistanceInKm()*getParams().getVat()*22400*0.2*1.5);
        return Math.max(val, getParams().getMinCap());
    }
}
