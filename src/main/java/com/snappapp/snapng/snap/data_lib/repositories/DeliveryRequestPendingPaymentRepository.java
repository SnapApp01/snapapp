package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequestPendingPayment;
import com.snappapp.snapng.snap.data_lib.enums.PendingPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRequestPendingPaymentRepository extends JpaRepository<DeliveryRequestPendingPayment,Long> {
    List<DeliveryRequestPendingPayment> findByStatusAndExpiryTimeBefore(PendingPaymentStatus status, LocalDateTime now);
    Optional<DeliveryRequestPendingPayment> findByRequest(DeliveryRequest request);
    List<DeliveryRequestPendingPayment> findByStatus(PendingPaymentStatus status);
}
