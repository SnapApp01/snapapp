package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransactionDto;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransaction;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

public interface WalletTransactionService {
    WalletTransaction startTransaction(CreateWalletTransactionDto dto);
    WalletTransaction completeTransaction(String ref);
    WalletTransaction cancelTransaction(String ref);
    WalletTransaction queryTransaction(String ref);
    List<WalletTransaction> getAllWithExternalRef(String externalRef);
    Page<WalletTransaction> getAll(String walletKey, int page, int size);
    default String generateReference(){
        return "TX|"+ IdUtilities.useDateTime()+"|"+ IdUtilities.shortUUID().toUpperCase();
    }
}
