package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {
    List<BankAccount> findByBusinessIdAndActiveTrue(String businessId);
    Optional<BankAccount> findFirstByBusinessId(String businessId);
    Optional<BankAccount> findFirstByBusinessIdAndAccountNumberAndBankCode(String businessId, String accountNumber, String bankCode);
}
