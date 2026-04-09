package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {
    List<BankAccount> findByBusinessIdAndActiveTrue(Long businessId);
    Optional<BankAccount> findFirstByBusinessId(Long businessId);
    Optional<BankAccount> findByBusinessIdAndAccountNumberAndBankCode(Long businessId, String accountNumber, String bankCode);
    Optional<BankAccount> findFirstByBusinessIdAndAccountNumberAndBankCode(Long businessId, String accountNumber, String bankCode);

    List<BankAccount> findByBusinessId(Long businessId);

    Optional<BankAccount> findByBusinessIdAndId(Long businessId, Long id);

    // Add these methods for recipient code management
    Optional<BankAccount> findByBankCodeAndAccountNumber(String bankCode, String accountNumber);

    @Query("SELECT b.recipientCode FROM BankAccount b WHERE b.bankCode = :bankCode AND b.accountNumber = :accountNumber")
    Optional<String> findRecipientCodeByBankCodeAndAccountNumber(@Param("bankCode") String bankCode,
                                                                 @Param("accountNumber") String accountNumber);

    @Modifying
    @Transactional
    @Query("UPDATE BankAccount b SET b.recipientCode = :recipientCode WHERE b.bankCode = :bankCode AND b.accountNumber = :accountNumber")
    void updateRecipientCode(@Param("bankCode") String bankCode,
                             @Param("accountNumber") String accountNumber,
                             @Param("recipientCode") String recipientCode);
}
