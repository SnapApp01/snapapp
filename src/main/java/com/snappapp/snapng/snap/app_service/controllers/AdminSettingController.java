package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.snap.data_lib.service.PlatformSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final PlatformSettingService platformSettingService;

    public AdminSettingController(PlatformSettingService platformSettingService) {
        this.platformSettingService = platformSettingService;
    }

    @PutMapping("/service-fee")
    public ResponseEntity<GenericResponse> updateServiceFee(
            @RequestParam BigDecimal percent) {

        GenericResponse genericResponse = platformSettingService.updateServiceFee(percent);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }
}
