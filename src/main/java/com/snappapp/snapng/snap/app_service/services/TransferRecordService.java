package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.snap.data_lib.entities.TransferRecord;
import com.snappapp.snapng.snap.data_lib.repositories.TransferRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferRecordService {
    
    private final TransferRecordRepository transferRecordRepository;
    
    public TransferRecord saveTransferRecord(TransferRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return transferRecordRepository.save(record);
    }
    
    public void updateTransferStatus(String reference, String status, String transferCode) {
        TransferRecord record = transferRecordRepository.findByReference(reference)
            .orElseThrow(() -> new RuntimeException("Transfer record not found: " + reference));
        record.setStatus(status);
        if (transferCode != null) {
            record.setTransferCode(transferCode);
        }
        record.setUpdatedAt(LocalDateTime.now());
        transferRecordRepository.save(record);
    }
    
    public void updateTransferFailure(String reference, String failureReason) {
        TransferRecord record = transferRecordRepository.findByReference(reference)
            .orElseThrow(() -> new RuntimeException("Transfer record not found: " + reference));
        record.setStatus("FAILED");
        record.setFailureReason(failureReason);
        record.setUpdatedAt(LocalDateTime.now());
        transferRecordRepository.save(record);
    }
    
    public TransferRecord getByReference(String reference) {
        return transferRecordRepository.findByReference(reference)
            .orElseThrow(() -> new RuntimeException("Transfer record not found: " + reference));
    }
}