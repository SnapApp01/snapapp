package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.enums.WalletType;
import com.snappapp.snapng.snap.app_service.apimodels.FundWalletRequest;
import com.snappapp.snapng.snap.app_service.apimodels.FundWalletResponse;
import com.snappapp.snapng.snap.app_service.apimodels.WalletTransactionResponse;
import com.snappapp.snapng.snap.app_service.apimodels.WithdrawalRequest;
import com.snappapp.snapng.snap.app_service.apimodels.transfer.*;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.utills.SecurityUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final WalletManagementService service;
    private final SecurityUtil securityUtil;

    public WalletController(WalletManagementService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/fund")
    public FundWalletResponse fund(@Validated @RequestBody FundWalletRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.initiateWalletFunding(request, user.getId());
    }

    @GetMapping("/transactions")
    public Page<WalletTransactionResponse> getTransactions(
            @RequestParam(defaultValue = "SNAP_USER") WalletType walletType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getTransactions(user.getId(), walletType, page, size);
    }

    @PostMapping("/withdraw")
    public void withdraw(@Validated @RequestBody WithdrawalRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        service.initiateWithdrawal(user.getId(), request);
    }

//     * Create a transfer recipient (bank account) on Paystack
//     * This is useful for saving beneficiary bank accounts
//     */
    @PostMapping("/transfer-recipient")
    public ResponseEntity<TransferRecipientResponse> createTransferRecipient(
            @Valid @RequestBody CreateTransferRecipientRequest request
    ) {
        log.info("Creating transfer recipient for account: {}", request.getAccountNumber());

        SnapUser user = securityUtil.getCurrentLoggedInUser();
        TransferRecipientResponse response = service.createTransferRecipient(request, user);

        return ResponseEntity.ok(response);
    }

    /**
     * Initiate a transfer to a saved recipient
     */
    @PostMapping("/initiate-transfer")
    public ResponseEntity<InitiateTransferResponse> initiateTransfer(
            @Valid @RequestBody InitiateTransferToRecipientRequest request
    ) {
        log.info("Initiating transfer to recipient: {}", request.getRecipientCode());

        SnapUser user = securityUtil.getCurrentLoggedInUser();
        InitiateTransferResponse response = service.initiateTransferToRecipient(request, user);

        return ResponseEntity.ok(response);
    }

    /**
     * Get list of all transfer recipients for the business
     */
    @GetMapping("/transfer-recipients")
    public ResponseEntity<List<TransferRecipientInfo>> getTransferRecipients() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        List<TransferRecipientInfo> recipients = service.getTransferRecipients(user);
        return ResponseEntity.ok(recipients);
    }

    /**
     * Check Paystack balance
     */
    @GetMapping("/paystack-balance")
    public ResponseEntity<BalanceResponse> getPaystackBalance() {
        BalanceResponse balance = service.getPaystackBalance();
        return ResponseEntity.ok(balance);
    }
}
