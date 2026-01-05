package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.snap.app_service.apimodels.*;
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
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import com.snappapp.snapng.snap.utils.utilities.InternalWalletUtilities;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletManagementService {

    private final SnapUserService userService;
    private final BusinessService businessService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;
    private final PaystackTransactionService paystackTransactionService;
    private final PaystackService paystackService;
    private final WalletTransferService transferService;
    private final DeliveryRequestService requestService;
    private final PushNotificationService notificationService;

    public WalletManagementService(SnapUserService userService, BusinessService businessService, WalletService walletService, WalletTransactionService walletTransactionService, PaystackTransactionService paystackTransactionService, PaystackService paystackService, WalletTransferService transferService, DeliveryRequestService requestService, PushNotificationService notificationService) {
        this.userService = userService;
        this.businessService = businessService;
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
        this.paystackTransactionService = paystackTransactionService;
        this.paystackService = paystackService;
        this.transferService = transferService;
        this.requestService = requestService;
        this.notificationService = notificationService;
    }

    public Page<WalletTransactionResponse> getTransactions(Long uid, int page, int size){
        SnapUser user = userService.withWallet(uid);
        Business business = businessService.getBusinessOfUser(user);
        String walletKey =  business==null ? user.getWalletKey() : business.getWalletKey();
        return walletTransactionService.getAll(walletKey, page,size).map(WalletTransactionResponse::new);
    }

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
                            .taskId(transaction.getReference())
                            .message(String.format("Your wallet has just been credited with ₦%.2f. Your wallet balance is now ₦%.2f",
                                    MoneyUtilities.fromMinorToBigDecimal(data.getAmount()).doubleValue(),MoneyUtilities.fromMinorToBigDecimal(creditWallet.getBookBalance()).doubleValue()))
                    .build());
        }
    }

    public void initiateWithdrawal(Long userId, WithdrawalRequest request){
        String walletKey = businessService.withWallet(userService.getUserById(userId)).getWalletKey();
        Wallet wallet = walletService.get(walletKey);
        Wallet creditWallet = walletService.get(InternalWalletUtilities.WALLET_WITHDRAWAL_PAYABLE);
        transferService.performFullTransfer(CreateWalletTransferDto
                .builder()
                .amount(request.getAmount())
                .debitWalletKey(wallet.getWalletKey())
                .creditWalletKey(creditWallet.getWalletKey())
                .reference(IdUtilities.useUUID())
                .narration("Withdrawal|" + request.getNarration())
                .build());
        notificationService.send(AddAppNotificationDto.builder()
                .title(NotificationTitle.TRANSACTION)
                .task(NotificationTask.RIDER_TRANSACTION.name())
                .taskId(wallet.getName())
                .message(String.format("Your wallet has just been debited with ₦%.2f. Your wallet balance is now ₦%.2f",
                        MoneyUtilities.fromMinorToBigDecimal(request.getAmount()).doubleValue(),MoneyUtilities.fromMinorToBigDecimal(creditWallet.getBookBalance()).doubleValue()))
                .build());
    }

    public void reverse(String ref){
        transferService.performReversal(ref);
    }
}
