package com.snappapp.snapng.snap.app_service.controllers;

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
    private final AppNotificationService notificationService;
    private final SecurityUtil securityUtil;

    public NotificationController(AppNotificationService notificationService, SecurityUtil securityUtil) {
        this.notificationService = notificationService;
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
    public List<AppNotificationResponse> getNotifications(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return notificationService.get(user.getId()).stream().map(AppNotificationResponse::new).toList();
    }

    @PutMapping("/read/{ref}")
    public void read(@PathVariable("ref")String ref){
        try{
            notificationService.update(ref);
        }
        catch (Exception e){
            log.info(e.getMessage());
        }
    }
}
