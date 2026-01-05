package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.snap.data_lib.dtos.BankAccountDto;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.repositories.BankAccountRepository;
import com.snappapp.snapng.snap.data_lib.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repo;

    public BankAccountServiceImpl(BankAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<BankAccount> get(String businessId) {
        return repo.findByBusinessIdAndActiveTrue(businessId);
    }

    @Override
    public BankAccount save(BankAccountDto dto, Business business) {
        BankAccount account = repo
                .findFirstByBusinessId(business.getCode()).orElse(new BankAccount());
        account.setAccountName(dto.getAccountName());
        account.setBankName(dto.getBankName());
        account.setAccountNumber(dto.getAccountNumber());
        account.setBusinessId(business.getCode());
        account.setBankCode(dto.getBankCode());
        account.setActive(true);
        return repo.save(account);
    }

    @Override
    public BankAccount deactivate(String accountNumber, String bankCode, String businessId) {
        BankAccount account = repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
        if(account == null)return null;
        account.setActive(false);
        return repo.save(account);
    }

    @Override
    public BankAccount activate(String accountNumber, String bankCode, String businessId) {
        BankAccount account = repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
        if(account == null)return null;
        account.setActive(true);
        return repo.save(account);
    }

    @Override
    public BankAccount get(String businessId, String accountNumber, String bankCode) {
        return repo
                .findFirstByBusinessIdAndAccountNumberAndBankCode(businessId,
                        accountNumber, bankCode).orElse(null);
    }
}
