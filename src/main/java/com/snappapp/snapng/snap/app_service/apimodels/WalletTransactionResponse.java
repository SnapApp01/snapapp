package com.snappapp.snapng.snap.app_service.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.WalletTransaction;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionStatus;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionType;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WalletTransactionResponse {

    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reference;
    private String narration;
    private WalletTransactionType type;
    private LocalDateTime transactionTime;
    private WalletTransactionStatus status;

    public WalletTransactionResponse(WalletTransaction transaction){
        this.balanceAfter = MoneyUtilities.fromMinorToBigDecimal(transaction.getBalanceAfter());
        this.balanceBefore = MoneyUtilities.fromMinorToBigDecimal(transaction.getBalanceBefore());
        this.amount = MoneyUtilities.fromMinorToBigDecimal(transaction.getAmount());
        this.narration = transaction.getNarration();
        this.reference = transaction.getReference();
        this.type = transaction.getTransactionType();
        this.transactionTime = transaction.getCreatedAt();
        this.status = transaction.getStatus();
    }
}
