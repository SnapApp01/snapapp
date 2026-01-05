package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
public class BankAccount extends BaseEntity {
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String businessId;
}
