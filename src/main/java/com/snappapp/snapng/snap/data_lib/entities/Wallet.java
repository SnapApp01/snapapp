package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "wallets")
@Data
public class Wallet extends BaseEntity {
    private String name;
    @Column(unique = true)
    private String walletKey;
    private Boolean canDebit = true;
    private Boolean canCredit = true;
    private Long bookBalance = 0L;
    private Long availableBalance = 0L;
}
