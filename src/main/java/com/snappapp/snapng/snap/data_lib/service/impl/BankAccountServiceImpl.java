package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.snap.data_lib.dtos.BankAccountDto;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.repositories.BankAccountRepository;
import com.snappapp.snapng.snap.data_lib.service.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repo;

    public BankAccountServiceImpl(BankAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<BankAccount> get(Long businessId) {
        return repo.findByBusinessIdAndActiveTrue(businessId);
    }

    @Override
    public BankAccount save(BankAccountDto dto, Business business) {

        // ✅ Check if account already exists
        Optional<BankAccount> existing = repo
                .findByBusinessIdAndAccountNumberAndBankCode(
                        business.getId(),
                        dto.getAccountNumber(),
                        dto.getBankCode()
                );

        if (existing.isPresent()) {
            throw new RuntimeException("Bank account already exists for this business");
        }

        // ✅ Save normally
        BankAccount account = BankAccount.builder()
                .accountNumber(dto.getAccountNumber())
                .bankName(dto.getBankName())
                .accountName(dto.getAccountName())
                .bankCode(dto.getBankCode())
                .businessId(business.getId())
                .build();
        account.setActive(true);
        return repo.save(account);
    }

//    // Add to BankAccountService
//    public String getRecipientCode(String bankCode, String accountNumber) {
//        // You might want to add a column to BankAccount entity for recipientCode
//        // or create a separate mapping table
//        return repo.findRecipientCodeByBankCodeAndAccountNumber(bankCode, accountNumber);
//    }
//
//    public void saveRecipientCode(String bankCode, String accountNumber, String recipientCode) {
//        // Update the bank account record with recipient code
//        repo.updateRecipientCode(bankCode, accountNumber, recipientCode);
//    }
//    @Override
//    public BankAccount save(BankAccountDto dto, Business business) {
//        BankAccount account = repo
//                .findFirstByBusinessId(business.getCode()).orElse(new BankAccount());
//        account.setAccountName(dto.getAccountName());
//        account.setBankName(dto.getBankName());
//        account.setAccountNumber(dto.getAccountNumber());
//        account.setBusinessId(business.getCode());
//        account.setBankCode(dto.getBankCode());
//        account.setActive(true);
//        return repo.save(account);
//    }

    @Override
    public BankAccount deactivate(String accountNumber, String bankCode, Long businessId) {
        BankAccount account = repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
        if(account == null)return null;
        account.setActive(false);
        return repo.save(account);
    }

    @Override
    public BankAccount activate(String accountNumber, String bankCode, Long businessId) {
        BankAccount account = repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
        if(account == null)return null;
        account.setActive(true);
        return repo.save(account);
    }

    @Override
    public BankAccount get(Long businessId, String accountNumber, String bankCode) {
        return repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
    }

    // Implement new methods for recipient code management

    @Override
    public String getRecipientCode(String bankCode, String accountNumber) {
        return repo
                .findRecipientCodeByBankCodeAndAccountNumber(bankCode, accountNumber)
                .orElse(null);
    }

    @Override
    @Transactional
    public void saveRecipientCode(String bankCode, String accountNumber, String recipientCode) {
        log.info("Saving recipient code {} for bank account {}/{}", recipientCode, bankCode, accountNumber);

        Optional<BankAccount> existingAccount = repo
                .findByBankCodeAndAccountNumber(bankCode, accountNumber);

        if (existingAccount.isPresent()) {
            BankAccount account = existingAccount.get();
            account.setRecipientCode(recipientCode);
            repo.save(account);
            log.info("Updated existing bank account with recipient code");
        } else {
            log.warn("Bank account not found for bankCode: {}, accountNumber: {}. Cannot save recipient code.",
                    bankCode, accountNumber);
            // Don't throw - just log, as the transfer can still proceed
        }
    }

    @Override
    public BankAccount findByBankCodeAndAccountNumber(String bankCode, String accountNumber) {
        return repo
                .findByBankCodeAndAccountNumber(bankCode, accountNumber)
                .orElse(null);
    }
}
