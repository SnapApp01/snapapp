package com.snappapp.snapng.snap.app_service.cron;

import com.snappapp.snapng.snap.app_service.services.PushNotificationService;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequestPendingPayment;
import com.snappapp.snapng.snap.data_lib.enums.DeliveryRequestStatus;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTask;
import com.snappapp.snapng.snap.data_lib.enums.NotificationTitle;
import com.snappapp.snapng.snap.data_lib.service.DeliveryPriceProposalService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestPendingPaymentService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPaymentCron {
    private final DeliveryPriceProposalService proposalService;
    private final DeliveryRequestService requestService;
    private final DeliveryRequestPendingPaymentService pendingPaymentService;
    private final WalletManagementService walletManagementService;
    private final PushNotificationService notificationService;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    

    @Scheduled(cron = "${service.auto-pay.cron.exp:0 * * * * *}")
    public void tryPendingPayments(){
        if(isRunning.get()){
            log.info("Cron running, cannot start new");
            return;
        }
        isRunning.set(true);
        log.info("Auto Pay Cron started");
        List<DeliveryRequestPendingPayment> pendingPayments = pendingPaymentService.findPending();
        for(DeliveryRequestPendingPayment payment : pendingPayments){
            try{
                walletManagementService.startRequestPayment(payment.getRequest());
                pendingPaymentService.markPaid(payment.getRequest());
                notificationService.send(AddAppNotificationDto.builder()
                        .message("You have just been credited for an awaiting delivery request")
                        .title(NotificationTitle.DELIVERY)
                        .uid(payment.getRequest().getBusinessUserId())
                        .task(NotificationTask.RIDER_DELIVERY.name())
                        .taskId(payment.getRequest().getTrackingId())
                        .build());
                notificationService.send(AddAppNotificationDto.builder()
                        .message("You have just been debited for an awaiting delivery request")
                        .title(NotificationTitle.DELIVERY)
                        .uid(payment.getRequest().getUser().getIdentifier())
                        .task(NotificationTask.USER_DELIVERY.name())
                        .taskId(payment.getRequest().getTrackingId())
                        .build());
            }
            catch (Exception e){
                log.info(e.getMessage());
            }
        }
        log.info("Auto Pay Cron ended");
        isRunning.set(false);
    }

    @Scheduled(cron = "${service.auto-cancel.cron.exp:30 * * * * *}")
    public void cancelExpired(){
        if(isRunning.get()){
            log.info("Cron running, cannot start new");
            return;
        }
        isRunning.set(true);
        log.info("Expired Pending Pay cancellation started");
        List<DeliveryRequestPendingPayment> pendingPayments = pendingPaymentService.expire();
        for(DeliveryRequestPendingPayment payment : pendingPayments){
            DeliveryRequest request = payment.getRequest();
            request.setStatus(DeliveryRequestStatus.CANCELED);
            requestService.updateStatus(request.getTrackingId(), DeliveryRequestStatus.CANCELED);
        }
        log.info("Expired Pending Pay cancellation ended");
        isRunning.set(false);
    }
}
