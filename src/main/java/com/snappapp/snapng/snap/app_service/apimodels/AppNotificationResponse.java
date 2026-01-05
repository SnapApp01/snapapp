package com.snappapp.snapng.snap.app_service.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppNotificationResponse {
    private String message;
    private String title;
    private String identifier;
    private String page;
    private String ref;
    private String time;

    public AppNotificationResponse(AppNotification notification){
        this.message = notification==null ? "":notification.getMessage();
        this.title = notification == null ? "" : notification.getTitle();
        this.ref = notification == null ? "" :notification.getReference();
        this.identifier = notification==null || notification.getTaskId()==null ? "": notification.getTaskId();
        this.page = notification==null || notification.getTask()==null ? "" : notification.getTask();
        this.time = notification==null || notification.getCreatedAt()==null ? "" : DateTimeUtils.timeAgo(notification.getCreatedAt());
    }
}
