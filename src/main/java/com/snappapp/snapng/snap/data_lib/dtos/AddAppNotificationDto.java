package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddAppNotificationDto {
    private String uid;
    private NotificationTitle title;
    private String message;
    private String task;
    private String taskId;
}
