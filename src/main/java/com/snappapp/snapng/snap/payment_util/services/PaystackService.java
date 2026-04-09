package com.snappapp.snapng.snap.payment_util.services;

import com.snappapp.snapng.snap.app_service.apimodels.transfer.*;
import com.snappapp.snapng.snap.payment_util.paystack.AccountEnquiryResponse;
import com.snappapp.snapng.snap.payment_util.paystack.BankResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitialPaymentResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitializePaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PaystackService {
    InitialPaymentResponse initializePayment(InitializePaymentRequest request);
    // Add these new transfer-related methods
    TransferRecipientResponse createTransferRecipient(TransferRecipientRequest request);
    InitiateTransferResponse initiateTransfer(InitiateTransferRequest request);
    TransferVerificationResponse verifyTransfer(String reference);
    AccountEnquiryResponse enquiryAccount(String accountNumber, String bankCode);
    List<BankResponse> getBanks();

    boolean verifySignature(String payload, String signature);
}
