package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransactionDto;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransaction;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionStatus;
import com.snappapp.snapng.snap.data_lib.enums.WalletTransactionType;
import com.snappapp.snapng.snap.data_lib.repositories.WalletTransactionRepository;
import com.snappapp.snapng.snap.data_lib.service.WalletTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {
    private final WalletTransactionRepository repo;

    public WalletTransactionServiceImpl(WalletTransactionRepository repo) {
        this.repo = repo;
    }

    @Override
    public WalletTransaction startTransaction(CreateWalletTransactionDto dto) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setStatus(WalletTransactionStatus.PENDING);
        transaction.setAmount(dto.getAmount());
        transaction.setExternalReference(dto.getRef());
        transaction.setNarration(dto.getNarration());
        transaction.setReference(generateReference());
        transaction.setWalletKey(dto.getWallet().getWalletKey());
        transaction.setTransactionType(dto.isDebit() ? WalletTransactionType.DEBIT : WalletTransactionType.CREDIT);
        transaction.setBalanceBefore(dto.getWallet().getBookBalance());
        int multiplier = dto.isDebit() ? -1 : 1;
        transaction.setBalanceAfter(dto.getWallet().getBookBalance() + (multiplier * dto.getAmount()));
        return repo.save(transaction);
    }

    @Override
    public WalletTransaction completeTransaction(String ref) {
        WalletTransaction transaction = queryTransaction(ref);
        if(!WalletTransactionStatus.PENDING.equals(transaction.getStatus())){
            throw new FailedProcessException("Transaction has already been completed or canceled");
        }
        transaction.setStatus(WalletTransactionStatus.COMPLETED);
        transaction.setValueTime(LocalDateTime.now());
        return repo.save(transaction);
    }

    @Override
    public WalletTransaction cancelTransaction(String ref) {
        WalletTransaction transaction = queryTransaction(ref);
        if(!WalletTransactionStatus.PENDING.equals(transaction.getStatus())){
            throw new FailedProcessException("Transaction has already been completed or canceled");
        }
        transaction.setStatus(WalletTransactionStatus.CANCELED);
        return repo.save(transaction);
    }

    @Override
    public WalletTransaction queryTransaction(String ref) {
        return repo.findByReference(ref).orElseThrow(()->new ResourceNotFoundException("Transaction does not exist"));
    }

    @Override
    public List<WalletTransaction> getAllWithExternalRef(String externalRef) {
        Page<WalletTransaction> page = repo.findByExternalReference(externalRef, PageRequest.of(0,25));
        return page.getContent();
    }

    @Override
    public Page<WalletTransaction> getAll(String walletKey, int page, int size) {
        return repo.findByWalletKey(walletKey, PageRequest.of(page,size, Sort.Direction.DESC,"createdAt"));
    }
}
