package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.WalletTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransferRepository extends JpaRepository<WalletTransfer, Long> {
    Optional<WalletTransfer> findFirstByExternalReference(String externalReference);
    Optional<WalletTransfer> findByTransferRefId(String transferRefId);
}
