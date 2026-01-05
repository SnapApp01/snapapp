package com.snappapp.snapng.snap.app_service.services;

import com.google.api.client.util.Strings;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class PushNotificationService {
    private ApnsConfig apnsConfig;
    private AndroidConfig androidConfig;
    private final SnapUserService userService;
    private final AppNotificationService appNotificationService;

    public PushNotificationService(SnapUserService userService, AppNotificationService appNotificationService) {
        this.userService = userService;
        this.appNotificationService = appNotificationService;
    }

    @PostConstruct
    public void init(){
        apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")  // Use "default" or specify the name of a custom sound
                        .build())
                .build();
        androidConfig = AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .setChannelId("snap_custom")// Specify "default" or the name of a custom sound file in res/raw/
                        .build())
                .build();
    }

    public void send(AddAppNotificationDto dto){
        AppNotification pn = appNotificationService.save(dto);
        CompletableFuture.runAsync(()->send(pn));
    }

    private void send(AppNotification pn){
        Notification notification = Notification.builder()
                .setBody(pn.getMessage())
                .setTitle(pn.getTitle())
                .build();

        Map<String,String> data = new HashMap<>();
        if(!Strings.isNullOrEmpty(pn.getTaskId())) {
            data.put("taskId", pn.getTaskId());
        }
        if(!Strings.isNullOrEmpty(pn.getTask())) {
            data.put("task", pn.getTask());
        }
        data.put("notificationId",pn.getReference());
        data.put("time", DateTimeUtils.timeAgo(pn.getCreatedAt()));
        Message message;
        try{
            if(Strings.isNullOrEmpty(pn.getTopic())){
                String token = userService.getUserByEmail(pn.getUid()).getDeviceToken();
                message = Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .putAllData(data)
                        .setApnsConfig(apnsConfig)
                        .setAndroidConfig(androidConfig)
                        .build();
            }
            else{
                message = Message.builder()
                        .setTopic(pn.getTopic())
                        .setNotification(notification)
                        .setApnsConfig(apnsConfig)
                        .setAndroidConfig(androidConfig)
                        .putAllData(data)
                        .build();
            }
            FirebaseMessaging.getInstance().send(message);
            appNotificationService.updateSentAt(List.of(pn.getId()));
        }
        catch (FirebaseMessagingException e) {
            appNotificationService.attempted(pn);
            log.warn("Failed to send "+pn.getReference());
        }
        catch (Exception e){
            appNotificationService.attempted(pn);
            log.warn("Error sending {}", pn.getReference(), e);
        }
    }

}
