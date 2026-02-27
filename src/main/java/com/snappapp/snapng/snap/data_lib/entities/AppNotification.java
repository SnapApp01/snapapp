package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.enums.NotificationType;
import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.converters.LocalDateTimeConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_notifications")
@Data
public class AppNotification extends BaseEntity {
    @Column(unique = true)
    private String reference;
    private String title;
    private String message;
    private String uid;
    private String identifier;
    private Boolean archived = false;
    private Boolean read = false;
    private String taskId;
    private String task;
    private String topic;
    /**
     * Type of notification to distinguish between user roles
     * USER - for customers/delivery senders
     * DRIVER - for business owners/drivers
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 20, nullable = false)
    private NotificationType notificationType = NotificationType.USER;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime sentAt;
    private Integer attempt = 0;
}
