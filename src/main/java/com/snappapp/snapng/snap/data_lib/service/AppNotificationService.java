package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.enums.NotificationOwnerType;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AppNotificationService {
    List<AppNotification> get(Long userId, NotificationOwnerType ownerType);

    //    List<AppNotification> get(Long id);
    AppNotification getLatest(String uid);
    AppNotification save(AddAppNotificationDto dto);
    void update(String reference);
    List<AppNotification> getPending(int size);
    void updateSentAt(List<Long> ids);
    void attempted(AppNotification pn);
}
