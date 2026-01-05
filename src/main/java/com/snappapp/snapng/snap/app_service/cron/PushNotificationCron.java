package com.snappapp.snapng.snap.app_service.cron;

import com.google.api.client.util.Strings;
import com.google.firebase.messaging.*;
import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushNotificationCron {
    private final AppNotificationService appNotificationService;
    private final SnapUserService userService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Scheduled(cron = "${service.push-notification.cron.exp:10 */2 * * * *}")
    public void job(){
        if(isRunning.get()){
            log.info("Push Notification Cron already running");
            return;
        }
        isRunning.set(true);
        log.info("Push Notification Cron started");
        List<AppNotification> pending = appNotificationService.getPending(5);
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")// Use "default" or specify the name of a custom sound
                        .build())
                .build();
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        //.setChannelId("snap_custom")// Specify "default" or the name of a custom sound file in res/raw/
                        .build())
                .build();
        for(AppNotification pn : pending){
            CompletableFuture.runAsync(()->send(apnsConfig,androidConfig,pn));
        }
        log.info("Push Notification Cron ended");
        isRunning.set(false);
    }

    private void send(ApnsConfig apnsConfig,AndroidConfig androidConfig,AppNotification pn){

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
            log.warn("Failed to send {}", pn.getReference(),e);
        }
        catch (Exception e){
            appNotificationService.attempted(pn);
            log.warn("Error sending {}", pn.getReference(),e);
        }
    }

}
