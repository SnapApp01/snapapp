package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.enums.NotificationOwnerType;
import com.snappapp.snapng.enums.NotificationType;
import com.snappapp.snapng.snap.app_service.apimodels.AppNotificationResponse;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.utills.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    @GetMapping("/latest")
//    public AppNotificationResponse getLatest(){
//        SnapUser user = securityUtil.getCurrentLoggedInUser();
//        AppNotificationResponse response = new AppNotificationResponse(null);
//        /*if(!Strings.isNullOrEmpty(response.getRef())) {
//            notificationService.update(response.getRef());
//        }*/
//        return response;
//    }

    @GetMapping("/latest")
    public AppNotificationResponse getLatest() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        AppNotificationResponse response = new AppNotificationResponse(
                appNotificationService.getLatest(user.getIdentifier())
        );
        return response;
    }

    /**
     * Get all notifications for current user
     * Can be filtered by type using query parameter
     *
     * @param type Optional - USER or DRIVER
     * @return List of notifications
     */
    @GetMapping
    public List<AppNotificationResponse> getNotifications(
            @RequestParam(required = false) NotificationType type
    ) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();

        if (type != null) {
            log.debug("Fetching {} notifications for user: {}", type, user.getEmail());
            return appNotificationService.getByUidAndType(user.getIdentifier(), type)
                    .stream()
                    .map(AppNotificationResponse::new)
                    .toList();
        }

        // Return all notifications if no type specified
        return appNotificationService.getByUid(user.getIdentifier())
                .stream()
                .map(AppNotificationResponse::new)
                .toList();
    }

    /**
     * Get notifications for user acting as customer/delivery sender
     */
    @GetMapping("/user")
    public List<AppNotificationResponse> getUserNotifications() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        log.debug("Fetching USER notifications for: {}", user.getEmail());

        return appNotificationService.getByUidAndType(user.getIdentifier(), NotificationType.USER)
                .stream()
                .map(AppNotificationResponse::new)
                .toList();
    }

    /**
     * Get notifications for user acting as driver/business owner
     */
    @GetMapping("/driver")
    public List<AppNotificationResponse> getDriverNotifications() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        log.debug("Fetching DRIVER notifications for: {}", user.getEmail());

        return appNotificationService.getByUidAndType(user.getIdentifier(), NotificationType.DRIVER)
                .stream()
                .map(AppNotificationResponse::new)
                .toList();
    }

    /**
     * Get unread notification counts by type
     * Useful for badge displays in mobile apps
     *
     * @return Map with counts for each notification type
     */
    @GetMapping("/counts")
    public Map<String, Long> getUnreadCounts() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();

        Map<String, Long> counts = new HashMap<>();
        counts.put("user", appNotificationService.countUnreadByType(
                user.getIdentifier(), NotificationType.USER));
        counts.put("driver", appNotificationService.countUnreadByType(
                user.getIdentifier(), NotificationType.DRIVER));
        counts.put("total", counts.get("user") + counts.get("driver"));

        log.debug("Notification counts for {}: {}", user.getEmail(), counts);
        return counts;
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/read/{ref}")
    public void read(@PathVariable("ref") String ref) {
        try {
            appNotificationService.update(ref);
            log.debug("Marked notification as read: {}", ref);
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", ref, e);
        }
    }

//    @GetMapping
//    public List<AppNotificationResponse> getNotifications(
//            @RequestParam(defaultValue = "SNAP_USER") NotificationOwnerType ownerType
//    ) {
//        SnapUser user = securityUtil.getCurrentLoggedInUser();
//
//        return appNotificationService
//                .get(user.getId(), ownerType)
//                .stream()
//                .map(AppNotificationResponse::new)
//                .toList();
//    }


//    @GetMapping
//    public List<AppNotificationResponse> getNotifications(){
//        SnapUser user = securityUtil.getCurrentLoggedInUser();
//        return appNotificationService.get(user.getId()).stream().map(AppNotificationResponse::new).toList();
//    }
//
//    @PutMapping("/read/{ref}")
//    public void read(@PathVariable("ref")String ref){
//        try{
//            appNotificationService.update(ref);
//        }
//        catch (Exception e){
//            log.info(e.getMessage());
//        }
//    }
}
