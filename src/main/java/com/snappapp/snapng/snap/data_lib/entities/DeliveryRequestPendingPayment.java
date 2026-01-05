package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.PendingPaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_request_pending_payments")
@Data
public class DeliveryRequestPendingPayment extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id",referencedColumnName = "id")
    private DeliveryRequest request;
    @Enumerated(EnumType.STRING)
    private PendingPaymentStatus status;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime expiryTime;
}
