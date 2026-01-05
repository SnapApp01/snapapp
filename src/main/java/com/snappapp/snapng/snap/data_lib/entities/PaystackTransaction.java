package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;

@Entity
@Table(name = "paystack_transactions")
@Data
public class PaystackTransaction extends BaseEntity {
    @Column(unique = true)
    private String reference;
    private String providerReference;
    private Long amount;
    private String narration;
    private String walletId;
    private String callbackUrl;
    private boolean completed;
    private boolean successful;
    @Column(length = 512)
    private String requestData;
    @Column(length = 512)
    private String responseData;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime completedAt;
}
