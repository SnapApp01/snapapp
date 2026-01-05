package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequestPendingPayment;
import com.snappapp.snapng.snap.data_lib.enums.PendingPaymentStatus;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestPendingPaymentRepository;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestPendingPaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeliveryRequestPendingPaymentServiceImpl implements DeliveryRequestPendingPaymentService {

    private final DeliveryRequestPendingPaymentRepository repo;

    public DeliveryRequestPendingPaymentServiceImpl(DeliveryRequestPendingPaymentRepository repo) {
        this.repo = repo;
    }

    @Override
    public DeliveryRequestPendingPayment create(DeliveryRequest request) {
        DeliveryRequestPendingPayment payment = new DeliveryRequestPendingPayment();
        payment.setRequest(request);
        payment.setExpiryTime(LocalDateTime.now().plusMinutes(30));
        payment.setStatus(PendingPaymentStatus.PENDING);
        return repo.save(payment);
    }

    @Override
    public List<DeliveryRequestPendingPayment> expire() {
        List<DeliveryRequestPendingPayment> ls = repo.findByStatusAndExpiryTimeBefore(PendingPaymentStatus.PENDING,LocalDateTime.now());
        ls.forEach(e->e.setStatus(PendingPaymentStatus.EXPIRED));
        return repo.saveAll(ls);
    }

    @Override
    public List<DeliveryRequestPendingPayment> findPending() {
        return repo.findByStatus(PendingPaymentStatus.PENDING);
    }


    @Override
    public DeliveryRequestPendingPayment markPaid(DeliveryRequest request) {
        Optional<DeliveryRequestPendingPayment> opt = repo.findByRequest(request);
        if(opt.isPresent()){
            DeliveryRequestPendingPayment payment = opt.get();
            payment.setStatus(PendingPaymentStatus.PAID);
            return repo.save(payment);
        }
        return null;
    }

    @Override
    public DeliveryRequestPendingPayment get(DeliveryRequest request) {
        return repo.findByRequest(request).orElse(null);
    }
}
