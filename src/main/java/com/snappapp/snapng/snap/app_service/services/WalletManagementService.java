package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.enums.NotificationType;
import com.snappapp.snapng.enums.WalletType;
import com.snappapp.snapng.exceptions.InsufficientBalanceException;
import com.snappapp.snapng.snap.app_service.apimodels.*;
import com.snappapp.snapng.snap.app_service.apimodels.transfer.*;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.dtos.CreatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.dtos.CreateWalletTransferDto;
import com.snappapp.snapng.snap.data_lib.dtos.UpdatePaystackTransactionDto;
import com.snappapp.snapng.snap.data_lib.entities.*;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTask;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import com.snappapp.snapng.snap.data_lib.service.*;
import com.snappapp.snapng.snap.payment_util.paystack.InitialPaymentResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitializePaymentRequest;
import com.snappapp.snapng.snap.payment_util.services.PaystackService;
import com.snappapp.snapng.snap.utils.utilities.InternalWalletUtilities;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class WalletManagementService {

    @Value("${paystack.secret-key}")
    private String paystackSecret;

    private final SnapUserService userService;
    private final BusinessService businessService;
    private final BankAccountService bankAccountService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;
    private final PaystackTransactionService paystackTransactionService;
    private final PaystackService paystackService;
    private final WalletTransferService transferService;
    private final DeliveryRequestService requestService;
    private final PushNotificationService notificationService;
    private final TransferRecordService transferRecordService;

    public WalletManagementService(SnapUserService userService, BusinessService businessService, BankAccountService bankAccountService, WalletService walletService, WalletTransactionService walletTransactionService, PaystackTransactionService paystackTransactionService, PaystackService paystackService, WalletTransferService transferService, DeliveryRequestService requestService, PushNotificationService notificationService, TransferRecordService transferRecordService) {
        this.userService = userService;
        this.businessService = businessService;
        this.bankAccountService = bankAccountService;
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
        this.paystackTransactionService = paystackTransactionService;
        this.paystackService = paystackService;
        this.transferService = transferService;
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.transferRecordService = transferRecordService;
    }

    @Transactional
    public Page<WalletTransactionResponse> getTransactions(
            Long uid,
            WalletType walletType,
            int page,
            int size
    ) {
        SnapUser user = userService.withWallet(uid);

        String walletKey = resolveWalletKey(user, walletType);

        return walletTransactionService
                .getAll(walletKey, page, size)
                .map(WalletTransactionResponse::new);
    }

    @Transactional
    public void startRequestPayment(DeliveryRequest request){
        if(!DeliveryRequestStatus.AWAITING_PAYMENT.equals(request.getStatus())){
            return;
        }
        transferService.performPartialTransfer(CreateWalletTransferDto
                .builder()
                        .amount(request.getAgreedFee())
                        .reference(request.getTrackingId())
                        .narration("Payment for request")
                        .debitWalletKey(request.getUser().getWalletKey())
                        .creditWalletKey(request.getBusiness().getWalletKey())
                .build());
        requestService.updateStatus(request.getTrackingId(),DeliveryRequestStatus.AWAITING_PICKUP);
    }

    public FundWalletResponse initiateWalletFunding(FundWalletRequest request, Long uid){
       SnapUser user = userService.withWallet(uid);
        Wallet wallet = walletService.get(user.getWalletKey());
        PaystackTransaction transaction = paystackTransactionService.create(
                CreatePaystackTransactionDto.builder()
                        .amount(request.getAmount())
                        .narration(request.getNarration())
                        .wallet(wallet)
                        .build());
        InitializePaymentRequest initializePaymentRequest = InitializePaymentRequest
                .builder()
                .email(user.getEmail())
                .reference(transaction.getReference())
                .callbackUrl("https://snapng.com")
                .amount(transaction.getAmount())
                .build();
        InitialPaymentResponse initialPaymentResponse = paystackService.initializePayment(initializePaymentRequest);
        paystackTransactionService.update(UpdatePaystackTransactionDto
                .builder()
                .reference(transaction.getReference())
                .providerRef(initialPaymentResponse.getAccessCode())
                .callbackUrl(initializePaymentRequest.getCallbackUrl())
                .build());
        return FundWalletResponse.builder()
                .provider("PAYSTACK")
                .url(initialPaymentResponse.getAuthorizationUrl())
                .build();
    }

    @Transactional
    public void initiateWithdrawal(Long userId, WithdrawalRequest request) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);

        // Get the business wallet
        Wallet debitWallet = walletService.get(business.getWalletKey());

        // Verify sufficient balance (amount is in kobo)
        if (debitWallet.getBookBalance() < request.getAmount()) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        try {
            // 1. Get or create transfer recipient for the bank account
            String recipientCode = getOrCreateTransferRecipient(request);

            // 2. Generate unique reference for this transfer
            String transferReference = generateTransferReference();

            // 3. Debit business wallet first (internal hold)
            Wallet internalCreditWallet = walletService.get(
                    InternalWalletUtilities.WALLET_WITHDRAWAL_PENDING
            );

            transferService.performFullTransfer(CreateWalletTransferDto.builder()
                    .amount(request.getAmount())
                    .debitWalletKey(debitWallet.getWalletKey())
                    .creditWalletKey(internalCreditWallet.getWalletKey())
                    .reference(transferReference)
                    .narration("Withdrawal initiated to bank | " + request.getBankAccountNumber())
                    .build());

            // 4. Initiate Paystack transfer
            InitiateTransferRequest transferRequest = InitiateTransferRequest.builder()
                    .source("balance")
                    .amount(request.getAmount())
                    .recipient(recipientCode)
                    .reference(transferReference)
                    .reason(request.getNarration() != null ? request.getNarration() : "Wallet withdrawal")
                    .build();

            InitiateTransferResponse transferResponse = paystackService.initiateTransfer(transferRequest);

            if (!transferResponse.isStatus()) {
                // Reverse internal transfer if Paystack initiation fails
                transferService.performReversal(transferReference);
                throw new RuntimeException("Transfer initiation failed: " + transferResponse.getMessage());
            }

            // 5. ⭐⭐⭐ CREATE THE TRANSFER RECORD HERE ⭐⭐⭐
            // This is where TransferRecord is created for later updates
            TransferRecord record = new TransferRecord();
            record.setReference(transferReference);
            record.setAmount(request.getAmount());
            record.setRecipientCode(recipientCode);
            record.setTransferCode(transferResponse.getData().getTransferCode());
            record.setStatus("PROCESSING"); // Initial status
            record.setBankAccountNumber(request.getBankAccountNumber());
            record.setBankCode(request.getBankCode());
            record.setAccountName(request.getAccountName());
            record.setBankName(request.getBankName());
            record.setUserId(userId);
            record.setBusinessId(business.getId());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());

            // Save to database
            transferRecordService.saveTransferRecord(record);

            // 6. Move from pending to processing
            Wallet processingWallet = walletService.get(
                    InternalWalletUtilities.WALLET_WITHDRAWAL_PROCESSING
            );
            transferService.performFullTransfer(CreateWalletTransferDto.builder()
                    .amount(request.getAmount())
                    .debitWalletKey(internalCreditWallet.getWalletKey())
                    .creditWalletKey(processingWallet.getWalletKey())
                    .reference(transferReference + "_processing")
                    .narration("Transfer submitted to Paystack")
                    .build());

            // Send notification about initiated withdrawal
            notificationService.send(AddAppNotificationDto.builder()
                    .title(NotificationTitle.TRANSACTION)
                    .task(NotificationTask.RIDER_DELIVERY.name())
                    .notificationType(NotificationType.USER)
                    .taskId(transferReference)
                    .message(String.format("Withdrawal of ₦%.2f has been initiated to account %s. You will be notified when completed.",
                            MoneyUtilities.fromMinorToBigDecimal(request.getAmount()).doubleValue(),
                            maskAccountNumber(request.getBankAccountNumber())))
                    .build());

        } catch (Exception e) {
            log.error("Withdrawal failed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Withdrawal processing failed", e);
        }
    }
    @Transactional
    public void handlePaystackSuccess(PaystackCallbackResponseData data) {
        log.info("Processing charge.success event");

        String reference = data.getReference();

        PaystackTransaction transaction =
                paystackTransactionService.get(reference);

        if (transaction.isCompleted()) {
            log.warn("Duplicate Paystack webhook for {}", reference);
            return;
        }

        boolean success =
                "success".equalsIgnoreCase(data.getStatus())
                        || "approved".equalsIgnoreCase(data.getGatewayResponse());

        if (!success) {
            return;
        }

        // 1️⃣ Update Paystack transaction (amount remains Long / kobo)
        paystackTransactionService.update(
                UpdatePaystackTransactionDto.builder()
                        .reference(reference)
                        .isSuccessful(true)
                        .responseData(data)
                        .build()
        );

        Wallet creditWallet =
                walletService.get(transaction.getWalletId());

        // 2️⃣ Perform wallet funding (external source)
        transferService.performFullTransfer(
                CreateWalletTransferDto.builder()
                        .amount(data.getAmount()) // ✅ Long (kobo)
                        .debitWalletKey(
                                InternalWalletUtilities.WALLET_FUNDING_RECEIVABLE
                        )
                        .creditWalletKey(
                                creditWallet.getWalletKey()
                        )
                        .reference(reference)
                        .narration("Funded via Paystack | " + data.getChannel())
                        .build()
        );
    }

    // Add these methods to your existing WalletManagementService

    @Transactional
    public void handleTransferSuccess(String reference, String transferCode, Long amount) {
        log.info("Transfer successful - reference: {}, transferCode: {}, amount: {}", reference, transferCode, amount);

        try {
            // Get the transfer record
            TransferRecord record = transferRecordService.getByReference(reference);

            // Move from processing wallet to completed wallet
            Wallet processingWallet = walletService.get(InternalWalletUtilities.WALLET_WITHDRAWAL_PROCESSING);
            Wallet completedWallet = walletService.get(InternalWalletUtilities.WALLET_WITHDRAWAL_COMPLETED);

            transferService.performFullTransfer(CreateWalletTransferDto.builder()
                    .amount(amount)
                    .debitWalletKey(processingWallet.getWalletKey())
                    .creditWalletKey(completedWallet.getWalletKey())
                    .reference(reference + "_completed")
                    .narration("Transfer completed successfully - " + transferCode)
                    .build());

            // Update transfer record status
            transferRecordService.updateTransferStatus(reference, "SUCCESS", transferCode);

            // Send success notification to user
            SnapUser user = userService.getUserById(record.getUserId());
            if (user != null) {
                notificationService.send(AddAppNotificationDto.builder()
                        .title(NotificationTitle.TRANSACTION)
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .notificationType(NotificationType.DRIVER)
                        .taskId(reference)
                        .message(String.format("✓ Withdrawal of ₦%.2f to account ending in %s has been completed successfully.",
                                MoneyUtilities.fromMinorToBigDecimal(amount).doubleValue(),
                                record.getBankAccountNumber().substring(Math.max(0, record.getBankAccountNumber().length() - 4))))
                        .build());
            }

            log.info("Transfer success fully processed for reference: {}", reference);

        } catch (Exception e) {
            log.error("Error processing transfer success for reference: {}", reference, e);
            // You might want to store this in a dead letter queue for manual processing
            throw new RuntimeException("Failed to process transfer success", e);
        }
    }

    @Transactional
    public void handleTransferFailure(String reference, String failureReason) {
        log.warn("Transfer failed - reference: {}, reason: {}", reference, failureReason);

        try {
            // Get the transfer record
            TransferRecord record = transferRecordService.getByReference(reference);

            // Reverse the entire transfer - move from processing back to business wallet
            // This performs a complete reversal of the original debit
            transferService.performReversal(reference);

            // Also move any remaining funds from processing wallet if needed
            Wallet processingWallet = walletService.get(InternalWalletUtilities.WALLET_WITHDRAWAL_PROCESSING);
            Wallet businessWallet = walletService.get(record.getBusinessId() != null ?
                    businessService.getBusinessById(record.getBusinessId()).getWalletKey() :
                    userService.getUserById(record.getUserId()).getWalletKey());

            // Update transfer record
            transferRecordService.updateTransferFailure(reference, failureReason);

            // Send failure notification to user
            SnapUser user = userService.getUserById(record.getUserId());
            if (user != null) {
                notificationService.send(AddAppNotificationDto.builder()
                        .title(NotificationTitle.TRANSACTION)
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .notificationType(NotificationType.USER)
                        .taskId(reference)
                        .message(String.format("✗ Withdrawal failed. Reason: %s. Amount of ₦%.2f has been returned to your wallet.",
                                failureReason,
                                MoneyUtilities.fromMinorToBigDecimal(record.getAmount()).doubleValue()))
                        .build());
            }

            log.info("Transfer failure processed and reversed for reference: {}", reference);

        } catch (Exception e) {
            log.error("Error processing transfer failure for reference: {}", reference, e);
            throw new RuntimeException("Failed to process transfer failure", e);
        }
    }

    @Transactional
    public void handleTransferReversal(String reference) {
        log.info("Transfer reversed - reference: {}", reference);

        try {
            // Update transfer record
            transferRecordService.updateTransferStatus(reference, "REVERSED", null);

            // Get record to send notification
            TransferRecord record = transferRecordService.getByReference(reference);

            // Send notification to user
            SnapUser user = userService.getUserById(record.getUserId());
            if (user != null) {
                notificationService.send(AddAppNotificationDto.builder()
                        .title(NotificationTitle.TRANSACTION)
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .notificationType(NotificationType.USER)
                        .taskId(reference)
                        .message("A previous withdrawal has been reversed by the bank. Please contact support for more information.")
                        .build());
            }

            log.info("Transfer reversal processed for reference: {}", reference);

        } catch (Exception e) {
            log.error("Error processing transfer reversal for reference: {}", reference, e);
            throw new RuntimeException("Failed to process transfer reversal", e);
        }
    }

    public void callback(PaystackCallbackResponseData data){
        String ref = data.getReference();
        PaystackTransaction transaction = paystackTransactionService.get(ref);
        if(transaction.isCompleted()){
            return;
        }
        boolean success = "success".equalsIgnoreCase(data.getStatus())
                || "Successful".equalsIgnoreCase(data.getGatewayResponse())
                || "Approved".equalsIgnoreCase(data.getGatewayResponse());
        transaction = paystackTransactionService.update(UpdatePaystackTransactionDto
                .builder()
                .reference(transaction.getReference())
                .isSuccessful(success)
                .responseData(data)
                .build());

        if(transaction.isSuccessful()) {
            Wallet creditWallet = walletService.get(transaction.getWalletId());
            Wallet debitWallet = walletService.get(InternalWalletUtilities.WALLET_FUNDING_RECEIVABLE);
            transferService.performFullTransfer(CreateWalletTransferDto
                    .builder()
                    .amount(data.getAmount())
                    .debitWalletKey(debitWallet.getWalletKey())
                    .creditWalletKey(creditWallet.getWalletKey())
                    .reference(transaction.getReference())
                    .narration("Funded via Paystack | " + data.getChannel())
                    .build());
            creditWallet = walletService.get(transaction.getWalletId());
            notificationService.send(AddAppNotificationDto.builder()
                            .title(NotificationTitle.TRANSACTION)
                            .task(NotificationTask.USER_TRANSACTION.name())
                            .notificationType(NotificationType.USER)
                            .taskId(transaction.getReference())
                            .message(String.format("Your wallet has just been credited with ₦%.2f. Your wallet balance is now ₦%.2f",
                                    MoneyUtilities.fromMinorToBigDecimal(data.getAmount()).doubleValue(),MoneyUtilities.fromMinorToBigDecimal(creditWallet.getBookBalance()).doubleValue()))
                    .build());
        }
    }

//    public void initiateWithdrawal(Long userId, WithdrawalRequest request){
//        String walletKey = businessService.withWallet(userService.getUserById(userId)).getWalletKey();
//        Wallet wallet = walletService.get(walletKey);
//        Wallet creditWallet = walletService.get(InternalWalletUtilities.WALLET_WITHDRAWAL_PAYABLE);
//        transferService.performFullTransfer(CreateWalletTransferDto
//                .builder()
//                .amount(request.getAmount())
//                .debitWalletKey(wallet.getWalletKey())
//                .creditWalletKey(creditWallet.getWalletKey())
//                .reference(IdUtilities.useUUID())
//                .narration("Withdrawal|" + request.getNarration())
//                .build());
//        notificationService.send(AddAppNotificationDto.builder()
//                .title(NotificationTitle.TRANSACTION)
//                .task(NotificationTask.RIDER_TRANSACTION.name())
//                .notificationType(NotificationType.DRIVER)
//                .taskId(wallet.getName())
//                .message(String.format("Your wallet has just been debited with ₦%.2f. Your wallet balance is now ₦%.2f",
//                        MoneyUtilities.fromMinorToBigDecimal(request.getAmount()).doubleValue(),MoneyUtilities.fromMinorToBigDecimal(creditWallet.getBookBalance()).doubleValue()))
//                .build());
//    }

    public void reverse(String ref){
        transferService.performReversal(ref);
    }

    public boolean verifyPaystackSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec =
                    new SecretKeySpec(paystackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveWalletKey(SnapUser user, WalletType walletType) {

        if (walletType == WalletType.SNAP_BUSINESS) {
            Business business = businessService.getBusinessOfUser(user);

            if (business == null) {
                throw new IllegalStateException(
                        "User does not have a business wallet"
                );
            }
            return business.getWalletKey();
        }
        return user.getWalletKey();
    }

    // Add to WalletManagementService.java

    private String getOrCreateTransferRecipient(WithdrawalRequest request) {
        // Check if we already have a recipient for this bank account
        String existingRecipient = bankAccountService.getRecipientCode(
                request.getBankCode(),
                request.getBankAccountNumber()
        );

        if (existingRecipient != null) {
            log.info("Using existing recipient code: {} for account: {}", existingRecipient, request.getBankAccountNumber());
            return existingRecipient;
        }

        // Create new recipient
        TransferRecipientRequest recipientRequest = TransferRecipientRequest.builder()
                .type("nuban")
                .name(request.getAccountName())
                .accountNumber(request.getBankAccountNumber())
                .bankCode(request.getBankCode())
                .currency("NGN")
                .build();

        TransferRecipientResponse response = paystackService.createTransferRecipient(recipientRequest);

        if (response == null || !response.isStatus()) {
            throw new RuntimeException("Failed to create transfer recipient: " +
                    (response != null ? response.getMessage() : "Unknown error"));
        }

        // Save recipient code to database
        bankAccountService.saveRecipientCode(
                request.getBankCode(),
                request.getBankAccountNumber(),
                response.getData().getRecipientCode()
        );

        log.info("Created new recipient code: {} for account: {}", response.getData().getRecipientCode(), request.getBankAccountNumber());
        return response.getData().getRecipientCode();
    }

    private String generateTransferReference() {
        return "WDL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 10) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
