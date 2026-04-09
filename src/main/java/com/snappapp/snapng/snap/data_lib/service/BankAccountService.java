package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.BankAccountDto;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BankAccountService {
    List<BankAccount> get(Long businessId);
    BankAccount save(BankAccountDto dto, Business business);
    BankAccount deactivate(String accountNumber, String bankCode, Long businessId);
    BankAccount activate(String accountNumber, String bankCode, Long businessId);
    BankAccount get(Long businessId, String accountNumber, String bankCode);
    // Add these new methods for recipient code management
    String getRecipientCode(String bankCode, String accountNumber);
    void saveRecipientCode(String bankCode, String accountNumber, String recipientCode);
    BankAccount findByBankCodeAndAccountNumber(String bankCode, String accountNumber);
}
