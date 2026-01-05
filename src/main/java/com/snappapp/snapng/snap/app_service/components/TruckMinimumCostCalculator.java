package com.snappapp.snapng.snap.app_service.components;

public class TruckMinimumCostCalculator extends MinimumCostCalculator {
    public TruckMinimumCostCalculator(CalculatorParams params) {
        super(params);
    }

    @Override
    public long calculate() {
        return (long)(getParams().getDistanceInKm() * 0.81 * getParams().getDieselPrice()
                * getParams().getProcessorCharges() * getParams().getVat() * (1.2+0.2+1));
    }
}
