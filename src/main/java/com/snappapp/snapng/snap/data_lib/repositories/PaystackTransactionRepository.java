package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.PaystackTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaystackTransactionRepository extends JpaRepository<PaystackTransaction,Long> {
    Optional<PaystackTransaction> findByReference(String reference);
    Page<PaystackTransaction> findByCompleted(boolean completed, Pageable pageable);
}
