package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.CreatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.dtos.UpdatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.entities.PaystackTransaction;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import org.springframework.stereotype.Component;

@Component
public interface PaystackTransactionService {
    PaystackTransaction get(String reference);
    PaystackTransaction create(CreatePaystackTransactionDto dto);
    PaystackTransaction update(UpdatePaystackTransactionDto dto);
    default String generateReference(){
        return "PST"+ IdUtilities.useDateTime()+IdUtilities.shortUUID().toUpperCase();
    }
}
