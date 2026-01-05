package com.snappapp.snapng.snap.data_lib.service;


import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransferDto;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransfer;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import org.springframework.stereotype.Component;

@Component
public interface WalletTransferService {
    WalletTransfer performFullTransfer(CreateWalletTransferDto dto);
    WalletTransfer performPartialTransfer(CreateWalletTransferDto dto);
    WalletTransfer completeTransfer(String transferRef);
    WalletTransfer performReversal(String transferRef);
    WalletTransfer getTransfer(String transferRef);
    WalletTransfer getTransferWithExternalRef(String externalRef);
    default String generateReference(){
        return "TRF|"+ IdUtilities.useDateTime()+"|"+IdUtilities.useUUID();
    }
}
