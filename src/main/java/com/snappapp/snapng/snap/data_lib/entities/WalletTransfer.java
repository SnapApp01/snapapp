package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transfers")
@Data
public class WalletTransfer extends BaseEntity {
    private String debitWallet;
    private String creditWallet;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime completedAt;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime reversedAt;
    private Long amount;
    private String narration;
    private String externalReference;
    @Column(unique = true)
    private String transferRefId;
    private String debitRef;
    private String creditRef;
    private String reversalCreditRef;
    private String reversalDebitRef;
    @Enumerated(EnumType.STRING)
    private TransferStatus debitStatus = TransferStatus.PENDING;
    @Enumerated(EnumType.STRING)
    private TransferStatus creditStatus = TransferStatus.PENDING;

    public Long getDebitAmount(){
        return -1*amount;
    }
}
