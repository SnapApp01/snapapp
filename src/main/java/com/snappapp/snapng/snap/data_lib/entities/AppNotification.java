package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.converters.LocalDateTimeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    private Boolean archived = false;
    private String taskId;
    private String task;
    private String topic;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime sentAt;
    private Integer attempt = 0;
}
