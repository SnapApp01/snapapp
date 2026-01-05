package com.snappapp.snapng.snap.app_service.components;

public class MotorcycleMinimumCostCalculator extends MinimumCostCalculator {
    public MotorcycleMinimumCostCalculator(CalculatorParams params) {
        super(params);
    }

    @Override
    public long calculate() {
        return (long)(this.getParams().getDistanceInKm() * 0.08 * this.getParams().getFuelPrice()
                * getParams().getProcessorCharges() * getParams().getVat() * (2+0.2+1));
    }
}
