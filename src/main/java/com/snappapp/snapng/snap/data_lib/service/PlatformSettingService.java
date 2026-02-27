package com.snappapp.snapng.snap.data_lib.service;


import com.snappapp.snapng.dto.GenericResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface PlatformSettingService {
    GenericResponse updateServiceFee(BigDecimal percent);
}
