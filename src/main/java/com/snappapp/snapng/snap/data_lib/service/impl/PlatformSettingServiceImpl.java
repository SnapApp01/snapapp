package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.snap.data_lib.entities.PlatformSetting;
import com.snappapp.snapng.snap.data_lib.repositories.PlatformSettingRepository;
import com.snappapp.snapng.snap.data_lib.service.PlatformSettingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlatformSettingServiceImpl implements PlatformSettingService {
    private final PlatformSettingRepository repository;

    public PlatformSettingServiceImpl(PlatformSettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public GenericResponse updateServiceFee(BigDecimal percent) {
        PlatformSetting setting = repository.findAll().get(0);
        setting.setDriverServiceFeePercent(percent);
        repository.save(setting);

        return GenericResponse.builder()
                .message("Service fee updated")
                .httpStatus(HttpStatus.OK)
                .isSuccess(true)
                .build();
    }
}
