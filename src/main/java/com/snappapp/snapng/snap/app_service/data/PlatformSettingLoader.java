package com.snappapp.snapng.snap.app_service.data;

import com.snappapp.snapng.snap.data_lib.entities.PlatformSetting;
import com.snappapp.snapng.snap.data_lib.repositories.PlatformSettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PlatformSettingLoader {

    private final PlatformSettingRepository repository;

    public PlatformSettingLoader(PlatformSettingRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void load() {
        if (repository.count() == 0) {
            PlatformSetting setting = new PlatformSetting();
            setting.setDriverServiceFeePercent(new BigDecimal("10"));
            repository.save(setting);
        }
    }
}
