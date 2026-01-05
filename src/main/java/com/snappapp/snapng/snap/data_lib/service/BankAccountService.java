package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.BankAccountDto;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BankAccountService {
    List<BankAccount> get(String businessId);
    BankAccount save(BankAccountDto dto, Business business);
    BankAccount deactivate(String accountNumber, String bankCode, String businessId);
    BankAccount activate(String accountNumber, String bankCode, String businessId);
    BankAccount get(String businessId, String accountNumber, String bankCode);
}
