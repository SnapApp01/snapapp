package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionStatus;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
public class WalletTransaction extends BaseEntity {
    private String walletKey;
    private Long amount;
    private String reference;
    private String externalReference;
    @Enumerated(EnumType.STRING)
    private WalletTransactionType transactionType;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime valueTime;
    private String narration;
    @Enumerated(EnumType.STRING)
    private WalletTransactionStatus status;
    private Long balanceBefore;
    private Long balanceAfter;
}
