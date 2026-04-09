package com.snappapp.snapng.snap.data_lib.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_records")
@Data
public class TransferRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String reference;
    
    private Long amount;
    
    private String recipientCode;
    
    private String transferCode;
    
    private String status; // PENDING, SUCCESS, FAILED, REVERSED
    
    private String bankAccountNumber;
    
    private String bankCode;
    
    private String accountName;
    
    private String bankName;
    
    private String failureReason;
    
    private Long userId;
    
    private Long businessId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}