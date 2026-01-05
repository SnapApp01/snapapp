package com.snappapp.snapng.snap.app_service.data;

import com.snappapp.snapng.exceptions.ResourceAlreadyExistsException;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import com.snappapp.snapng.snap.utils.utilities.InternalWalletUtilities;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class InternalWalletLoader {
    private final WalletService walletService;

    @PostConstruct
    public void init(){
        try {
            walletService.createInternal("Wallet Funding Receivable", InternalWalletUtilities.WALLET_FUNDING_RECEIVABLE,99999999999999L);
        }catch (ResourceAlreadyExistsException e){
            log.warn(e.getMessage());
        }
        try {
            walletService.createInternal("Wallet Withdrawal Receivable", InternalWalletUtilities.WALLET_WITHDRAWAL_PAYABLE,0L);
        }catch (ResourceAlreadyExistsException e){
            log.warn(e.getMessage());
        }
    }
}
