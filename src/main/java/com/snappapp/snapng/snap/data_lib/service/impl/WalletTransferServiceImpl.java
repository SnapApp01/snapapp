package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransactionDto;
import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransferDto;
import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransaction;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransfer;
import com.snappapp.snapng.snap.data_lib.enums.TransferStatus;
import com.snappapp.snapng.snap.data_lib.repositories.WalletTransferRepository;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import com.snappapp.snapng.snap.data_lib.service.WalletTransactionService;
import com.snappapp.snapng.snap.data_lib.service.WalletTransferService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class WalletTransferServiceImpl implements WalletTransferService {
    private final WalletTransferRepository repo;
    private final WalletTransactionService transactionService;
    private final WalletService walletService;

    public WalletTransferServiceImpl(WalletTransferRepository repo, WalletTransactionService transactionService, WalletService walletService) {
        this.repo = repo;
        this.transactionService = transactionService;
        this.walletService = walletService;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public WalletTransfer performFullTransfer(CreateWalletTransferDto request) {

        Long amount = request.getAmount();          // +10000
        Long debitAmount = request.getDebitAmount(); // -10000

        // 1️⃣ CREDIT wallet (no balance validation for credit)
        Wallet creditWallet =
                walletService.validateBalances(
                        amount,
                        request.getCreditWalletKey(),
                        false, // credit
                        true
                );

        // 2️⃣ DEBIT wallet
        Wallet debitWallet =
                walletService.validateBalances(
                        debitAmount,
                        request.getDebitWalletKey(),
                        true,   // debit
                        true
                );

        // 3️⃣ Create transfer
        WalletTransfer transfer = createWalletTransfer(request);
        transfer.setCreditWallet(creditWallet.getWalletKey());
        transfer.setDebitWallet(debitWallet.getWalletKey());

        // 4️⃣ Debit transaction
        WalletTransaction debitTransaction =
                transactionService.startTransaction(
                        CreateWalletTransactionDto.builder()
                                .wallet(debitWallet)
                                .isDebit(true)
                                .amount(debitAmount) // NEGATIVE
                                .ref(transfer.getTransferRefId())
                                .narration(request.getNarration())
                                .build()
                );

        // 5️⃣ Credit transaction
        WalletTransaction creditTransaction =
                transactionService.startTransaction(
                        CreateWalletTransactionDto.builder()
                                .wallet(creditWallet)
                                .isDebit(false)
                                .amount(amount) // POSITIVE
                                .ref(transfer.getTransferRefId())
                                .narration(request.getNarration())
                                .build()
                );

        transfer.setDebitRef(debitTransaction.getReference());
        transfer.setCreditRef(creditTransaction.getReference());

        // 6️⃣ Complete transactions
        transactionService.completeTransaction(debitTransaction.getReference());
        transactionService.completeTransaction(creditTransaction.getReference());

        // 7️⃣ Update balances (SIGNED amounts)
        walletService.updateBalances(
                debitAmount,
                request.getDebitWalletKey(),
                true,
                true
        );
        walletService.updateBalances(
                amount,
                request.getCreditWalletKey(),
                false,
                true
        );

        transfer.setDebitStatus(TransferStatus.COMPLETE);
        transfer.setCreditStatus(TransferStatus.COMPLETE);
        transfer.setCompletedAt(LocalDateTime.now());

        return repo.save(transfer);
    }

    @Override
    @Transactional
    public WalletTransfer performPartialTransfer(CreateWalletTransferDto request) {
        Wallet debitWallet = walletService.validateBalances(request.getDebitAmount(),request.getDebitWalletKey(),true,true);
        Wallet creditWallet = walletService.validateBalances(request.getAmount(),request.getCreditWalletKey(),true,true);
        WalletTransfer transfer = createWalletTransfer(request);
        transfer.setCreditWallet(creditWallet.getWalletKey());
        transfer.setDebitWallet(debitWallet.getWalletKey());
        WalletTransaction debitTransaction = transactionService.startTransaction(CreateWalletTransactionDto
                .builder()
                .amount(request.getAmount())
                .wallet(debitWallet)
                .isDebit(true)
                .narration(request.getNarration())
                .ref(transfer.getTransferRefId())
                .build());
        WalletTransaction creditTransaction = transactionService.startTransaction(CreateWalletTransactionDto
                .builder()
                .wallet(creditWallet)
                .isDebit(false)
                .ref(transfer.getTransferRefId())
                .narration(request.getNarration())
                .amount(request.getAmount())
                .build());
        walletService.updateBalances(request.getDebitAmount(),request.getDebitWalletKey(),false,true);
        walletService.updateBalances(request.getAmount(),request.getCreditWalletKey(),true,false);
        transfer.setCreditRef(creditTransaction.getReference());
        transfer.setDebitRef(debitTransaction.getReference());
        return repo.save(transfer);
    }

    @Override
    @Transactional
    public WalletTransfer completeTransfer(String transferRef) {
        WalletTransfer transfer = getTransfer(transferRef);
        incomplete(transfer);
        walletService.validateBalances(transfer.getDebitAmount(),transfer.getDebitWallet(),true,false);
        walletService.validateBalances(transfer.getAmount(),transfer.getCreditWallet(),false,true);
        transactionService.completeTransaction(transfer.getDebitRef());
        transactionService.completeTransaction(transfer.getCreditRef());
        walletService.updateBalances(transfer.getDebitAmount(),transfer.getDebitWallet(),true,false);
        walletService.updateBalances(transfer.getAmount(),transfer.getCreditWallet(),false,true);
        transfer.setCreditStatus(TransferStatus.COMPLETE);
        transfer.setDebitStatus(TransferStatus.COMPLETE);
        transfer.setCompletedAt(LocalDateTime.now());
        return repo.save(transfer);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public WalletTransfer performReversal(String transferRef) {
        WalletTransfer transfer = getTransfer(transferRef);
        canDoReversal(transfer);
        return doReversal(transfer);
    }

    @Override
    public WalletTransfer getTransfer(String transferRef) {
        return repo.findByTransferRefId(transferRef).orElseThrow(()->new ResourceNotFoundException("Transfer is not valid"));
    }

    @Override
    public WalletTransfer getTransferWithExternalRef(String externalRef) {
        return repo.findFirstByExternalReference(externalRef).orElseThrow(()->new ResourceNotFoundException("Transfer not found"));
    }

    private void incomplete(WalletTransfer walletTransfer){
        if(TransferStatus.COMPLETE.equals(walletTransfer.getCreditStatus()) && TransferStatus.COMPLETE.equals(walletTransfer.getDebitStatus())){
            throw new FailedProcessException("Transaction already completed");
        }
        if(TransferStatus.REVERSED.equals(walletTransfer.getDebitStatus()) || TransferStatus.REVERSED.equals(walletTransfer.getCreditStatus())){
            throw new FailedProcessException("Transaction already reversed");
        }
    }

    private void canDoReversal(WalletTransfer walletTransfer){
        if(TransferStatus.REVERSED.equals(walletTransfer.getDebitStatus()) || TransferStatus.REVERSED.equals(walletTransfer.getCreditStatus())){
            throw new FailedProcessException("Transaction already reversed");
        }
        if(TransferStatus.PENDING.equals(walletTransfer.getDebitStatus())){
            transactionService.cancelTransaction(walletTransfer.getDebitRef());
        }
        if(TransferStatus.PENDING.equals(walletTransfer.getCreditStatus())){
            transactionService.cancelTransaction(walletTransfer.getCreditRef());
        }
    }

    private WalletTransfer doReversal(WalletTransfer transfer){
        boolean isDebitComplete = TransferStatus.COMPLETE.equals(transfer.getDebitStatus());
        boolean isCreditComplete = TransferStatus.COMPLETE.equals(transfer.getCreditStatus());
        Wallet debitWallet = walletService.validateBalances(transfer.getDebitAmount(),transfer.getCreditWallet(),true,isCreditComplete);
        Wallet creditWallet = walletService.validateBalances(transfer.getAmount(),transfer.getDebitWallet(),isDebitComplete,true);
        WalletTransaction debitTransaction = transactionService.startTransaction(CreateWalletTransactionDto
                .builder()
                .amount(transfer.getAmount())
                .wallet(debitWallet)
                .isDebit(true)
                .narration("RVSL|"+transfer.getNarration())
                .ref(transfer.getTransferRefId())
                .build());
        WalletTransaction creditTransaction = transactionService.startTransaction(CreateWalletTransactionDto
                .builder()
                .wallet(creditWallet)
                .isDebit(false)
                .ref(transfer.getTransferRefId())
                .narration("RVSL|"+transfer.getNarration())
                .amount(transfer.getAmount())
                .build());
        transfer.setReversalCreditRef(creditTransaction.getReference());
        transfer.setReversalDebitRef(debitTransaction.getReference());
        transactionService.completeTransaction(transfer.getReversalDebitRef());
        transactionService.completeTransaction(transfer.getReversalCreditRef());
        walletService.updateBalances(transfer.getDebitAmount(),transfer.getCreditWallet(),true,isCreditComplete);
        walletService.updateBalances(transfer.getAmount(),transfer.getDebitWallet(),isDebitComplete,true);
        transfer.setCreditStatus(TransferStatus.REVERSED);
        transfer.setDebitStatus(TransferStatus.REVERSED);
        transfer.setReversedAt(LocalDateTime.now());
        return repo.save(transfer);
    }

    private WalletTransfer createWalletTransfer(CreateWalletTransferDto dto){
        WalletTransfer transfer = new WalletTransfer();
        transfer.setTransferRefId(generateReference());
        transfer.setAmount(dto.getAmount());
        transfer.setDebitWallet(dto.getDebitWalletKey());
        transfer.setCreditWallet(dto.getCreditWalletKey());
        transfer.setExternalReference(dto.getReference());
        transfer.setNarration(dto.getNarration());
        return repo.save(transfer);
    }
}
