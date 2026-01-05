package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.utils.utilities.StringUtilities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public interface WalletService {
    Wallet save(String name);
    Wallet createInternal(String name, String key,Long initialBalance);
    Wallet save(String walletKey,boolean canDebit, boolean canCredit);
    Wallet updateCreditStatus(String walletKey, boolean canCredit);
    Wallet updateDebitStatus(String walletKey, boolean canDebit);
    Wallet get(String walletKey);
    Page<Wallet> get(Pageable pageable);
    Wallet updateBalances(Long amount, String key, boolean book, boolean available);
    Wallet validateBalances(Long amount, String key, boolean book, boolean available);

    default String generateKey(){
        return StringUtilities.leftPadZeroes(new SecureRandom().nextInt(999999),10);
    }
}
