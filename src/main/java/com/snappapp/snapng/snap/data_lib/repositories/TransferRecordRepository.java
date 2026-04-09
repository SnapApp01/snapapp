package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {
    Optional<TransferRecord> findByReference(String reference);
}