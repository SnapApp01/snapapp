package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.enums.NotificationOwnerType;
import com.snappapp.snapng.snap.app_service.apimodels.AppNotificationResponse;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.utills.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {
    private final AppNotificationService appNotificationService;
    private final SecurityUtil securityUtil;

    public NotificationController(AppNotificationService appNotificationService, SecurityUtil securityUtil) {
        this.appNotificationService = appNotificationService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/latest")
    public AppNotificationResponse getLatest(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        AppNotificationResponse response = new AppNotificationResponse(null);
        /*if(!Strings.isNullOrEmpty(response.getRef())) {
            notificationService.update(response.getRef());
        }*/
        return response;
    }

    @GetMapping
    public List<AppNotificationResponse> getNotifications(
            @RequestParam(defaultValue = "SNAP_USER") NotificationOwnerType ownerType
    ) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();

        return appNotificationService
                .get(user.getId(), ownerType)
                .stream()
                .map(AppNotificationResponse::new)
                .toList();
    }


//    @GetMapping
//    public List<AppNotificationResponse> getNotifications(){
//        SnapUser user = securityUtil.getCurrentLoggedInUser();
//        return appNotificationService.get(user.getId()).stream().map(AppNotificationResponse::new).toList();
//    }

    @PutMapping("/read/{ref}")
    public void read(@PathVariable("ref")String ref){
        try{
            appNotificationService.update(ref);
        }
        catch (Exception e){
            log.info(e.getMessage());
        }
    }
}
