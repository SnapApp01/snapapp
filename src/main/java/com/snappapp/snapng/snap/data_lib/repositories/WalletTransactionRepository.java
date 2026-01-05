package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction,Long> {
    Page<WalletTransaction> findByWalletKey(String walletKey, Pageable pageable);
    Page<WalletTransaction> findByExternalReference(String externalReference, Pageable pageable);
    Optional<WalletTransaction> findByReference(String reference);
}
