package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.gson.Gson;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.CreatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.dtos.UpdatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.entities.PaystackTransaction;
import com.snappapp.snapng.snap.data_lib.repositories.PaystackTransactionRepository;
import com.snappapp.snapng.snap.data_lib.service.PaystackTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaystackTransactionServiceImpl implements PaystackTransactionService {
    private final PaystackTransactionRepository repo;

    public PaystackTransactionServiceImpl(PaystackTransactionRepository repo) {
        this.repo = repo;
    }

    @Override
    public PaystackTransaction get(String reference) {
        return repo.findByReference(reference).orElseThrow(()-> new ResourceNotFoundException("Paystack Transaction not found"));
    }

    @Override
    public PaystackTransaction create(CreatePaystackTransactionDto dto) {
        PaystackTransaction transaction = new PaystackTransaction();
        transaction.setAmount(dto.getAmount());
        transaction.setWalletId(dto.getWallet().getWalletKey());
        transaction.setReference(generateReference());
        transaction.setNarration(dto.getNarration());
        transaction.setRequestData(new Gson().toJson(dto.getData()));
        return repo.save(transaction);
    }

    @Override
    public PaystackTransaction update(UpdatePaystackTransactionDto dto) {
        PaystackTransaction transaction = get(dto.getReference());
        transaction.setProviderReference(dto.getProviderRef());
        if(dto.getCallbackUrl()!=null){
            transaction.setCallbackUrl(dto.getCallbackUrl());
        }
        if(dto.getIsSuccessful()!=null){
            transaction.setSuccessful(dto.getIsSuccessful());
            transaction.setCompleted(true);
            transaction.setCompletedAt(LocalDateTime.now());
        }
        if(dto.getResponseData()!=null) {
            transaction.setResponseData(new Gson().toJson(dto.getResponseData()));
        }
        return repo.save(transaction);
    }
}
