package com.snappapp.snapng.snap.payment_util.services;

import com.snappapp.snapng.snap.app_service.apimodels.transfer.*;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PaystackServiceImpl implements PaystackService {

    @Value("${paystack.base-url}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;

    private PaystackClient client;
    private final RestTemplate restTemplate;

    public PaystackServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

    @Override
    public TransferRecipientResponse createTransferRecipient(TransferRecipientRequest request) {
        String url = "https://api.paystack.co/transferrecipient";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("type", request.getType());
        body.put("name", request.getName());
        body.put("account_number", request.getAccountNumber());
        body.put("bank_code", request.getBankCode());
        body.put("currency", request.getCurrency());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TransferRecipientResponse> response = restTemplate.postForEntity(
                    url, entity, TransferRecipientResponse.class
            );

            if (response.getBody() == null) {
                log.error("Null response from Paystack create transfer recipient");
                throw new RuntimeException("Null response from Paystack");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to create transfer recipient", e);
            throw new RuntimeException("Paystack create recipient failed: " + e.getMessage(), e);
        }
    }

    @Override
    public InitiateTransferResponse initiateTransfer(InitiateTransferRequest request) {
        String url = "https://api.paystack.co/transfer";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InitiateTransferRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<InitiateTransferResponse> response = restTemplate.postForEntity(
                    url, entity, InitiateTransferResponse.class
            );

            if (response.getBody() == null) {
                log.error("Null response from Paystack initiate transfer");
                throw new RuntimeException("Null response from Paystack");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to initiate transfer", e);
            throw new RuntimeException("Paystack transfer initiation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TransferVerificationResponse verifyTransfer(String reference) {
        String url = "https://api.paystack.co/transfer/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<TransferVerificationResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, TransferVerificationResponse.class
            );

            if (response.getBody() == null) {
                log.error("Null response from Paystack verify transfer for reference: {}", reference);
                throw new RuntimeException("Null response from Paystack");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to verify transfer for reference: {}", reference, e);
            throw new RuntimeException("Paystack transfer verification failed: " + e.getMessage(), e);
        }
    }
}