package com.snappapp.snapng.snap.payment_util.services;

import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransferDto;
import com.snappapp.snapng.snap.payment_util.clients.PaystackClient;
import com.snappapp.snapng.snap.payment_util.paystack.AccountEnquiryResponse;
import com.snappapp.snapng.snap.payment_util.paystack.BankResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitialPaymentResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitializePaymentRequest;
import com.snappapp.snapng.snap.payment_util.services.PaystackService;
import com.snappapp.snapng.snap.utils.utilities.InternalWalletUtilities;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

@Service
@Slf4j
public class PaystackServiceImpl implements PaystackService {

    @Value("${paystack.base-url}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;

    private PaystackClient client;

    @PostConstruct
    public void initSetup() {
        log.info("Initializing Paystack service with base URL: {}", baseUrl);
        client = PaystackClient.builder()
                .baseUrl(baseUrl)
                .secretKey(secretKey)
                .restTemplate(new RestTemplate())
                .build();
        log.info("Paystack client initialized successfully.");
    }

    @Override
    public InitialPaymentResponse initializePayment(InitializePaymentRequest request) {
        log.info("Initializing payment request for email: {}", request.getEmail());
        try {
            InitialPaymentResponse response = client.makeRequest(HttpMethod.POST, request, "/transaction/initialize", InitialPaymentResponse.class);
            log.info("Payment initialized successfully with reference: {}", response.getReference());
            return response;
        } catch (Exception e) {
            log.error("Error initializing payment for email: {} - Exception: {}", request.getEmail(), e.getMessage(), e);
            throw e;  // Re-throw or handle appropriately
        }
    }

    @Override
    public AccountEnquiryResponse enquiryAccount(String accountNumber, String bankCode) {
        log.info("Enquiring account for account number: {} and bank code: {}", accountNumber, bankCode);
        try {
            AccountEnquiryResponse response = client.makeRequest(HttpMethod.GET, null, String.format("/bank/resolve?account_number=%s&bank_code=%s", accountNumber, bankCode));
            log.info("Account enquiry successful for account number: {}", accountNumber);
            return response;
        } catch (Exception e) {
            log.error("Error enquiring account for account number: {} - Exception: {}", accountNumber, e.getMessage(), e);
            throw e;  // Re-throw or handle appropriately
        }
    }

    @Override
    public List<BankResponse> getBanks() {
        log.info("Fetching list of available banks from Paystack.");
        try {
            List<BankResponse> banks = client.makeRequest();
            log.info("Fetched {} banks from Paystack.", banks.size());
            return banks;
        } catch (Exception e) {
            log.error("Error fetching bank list from Paystack - Exception: {}", e.getMessage(), e);
            throw e;  // Re-throw or handle appropriately
        }
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec key =
                    new SecretKeySpec(
                            secretKey.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA512"
                    );

            mac.init(key);

            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

            String calculated =
                    HexFormat.of().formatHex(hash);

            return calculated.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.warn("Failed to verify Paystack signature", e);
            return false;
        }
    }
}