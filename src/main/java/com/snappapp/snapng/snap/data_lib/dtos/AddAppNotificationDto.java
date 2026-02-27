package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.enums.NotificationType;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddAppNotificationDto {
    private String message;
    private NotificationTitle title;
    private String uid;
    private String taskId;
    private String task;

    /**
     * Type of notification based on user role
     * Defaults to USER if not specified for backward compatibility
     */
    @Builder.Default
    private NotificationType notificationType = NotificationType.USER;
}