package com.snappapp.snapng.snap.payment_util.services;

import com.snappapp.snapng.snap.payment_util.paystack.AccountEnquiryResponse;
import com.snappapp.snapng.snap.payment_util.paystack.BankResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitialPaymentResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitializePaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PaystackService {
    InitialPaymentResponse initializePayment(InitializePaymentRequest request);
    AccountEnquiryResponse enquiryAccount(String accountNumber, String bankCode);
    List<BankResponse> getBanks();
}
