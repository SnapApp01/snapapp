package com.snappapp.snapng.snap.app_service.components;

import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class MinimumCostCalculator {
    private CalculatorParams params;
    public static MinimumCostCalculator getInstance(VehicleType vehicleType, CalculatorParams params){
        switch (vehicleType){
            case CAR:
                return new CarMinimumCostCalculator(params);
            case VAN:
                return new VanMinimumCostCalculator(params);
            case TRUCK:
                return new TruckMinimumCostCalculator(params);
            case BICYCLE:
                return new BicycleMinimumCostCalculator(params);
            case MOTORCYCLE:
                return new MotorcycleMinimumCostCalculator(params);
            default:
                return null;
        }
    }

    public abstract long calculate();
}
