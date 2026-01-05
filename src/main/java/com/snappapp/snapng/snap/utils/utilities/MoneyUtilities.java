package com.snappapp.snapng.snap.utils.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtilities {
    private static final Long rate = 100L;

    public static BigDecimal fromMinorToBigDecimal(Long minorVal){
        return minorVal==null?BigDecimal.ZERO:
                BigDecimal.valueOf(minorVal).divide(BigDecimal.valueOf(rate),2, RoundingMode.CEILING);
    }


    public static Double fromMinorToDouble(Long minorVal){
        return minorVal==null ? 0 : ((double)minorVal / rate);
    }

    public static Long fromDoubleToMinor(Double doubleVal){
        return doubleVal==null?0: (long)(doubleVal * rate.doubleValue());
    }
}
