package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequestPendingPayment;

import java.util.List;

public interface DeliveryRequestPendingPaymentService {
    DeliveryRequestPendingPayment create(DeliveryRequest request);
    List<DeliveryRequestPendingPayment> expire();
    List<DeliveryRequestPendingPayment> findPending();
    DeliveryRequestPendingPayment markPaid(DeliveryRequest request);
    DeliveryRequestPendingPayment get(DeliveryRequest request);
}
