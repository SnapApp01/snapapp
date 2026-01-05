package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.converters.LocalDateTimeConverter;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_request_stages")
@Data
public class DeliveryRequestStage extends BaseEntity {
    private String deliveryRequest;
    private DeliveryRequestStatus status;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime completedAt;
    //@Convert(converter = MapToJsonConverter.class)
    //private Map<String,String> data;
}
