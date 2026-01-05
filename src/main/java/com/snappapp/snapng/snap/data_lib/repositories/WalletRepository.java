package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByWalletKey(String key);

    boolean existsByWalletKey(String key);

    @Modifying
    @Query("UPDATE Wallet w SET w.bookBalance = w.bookBalance + :amount, " +
            "w.availableBalance = w.availableBalance + :amount WHERE w.walletKey = :key")
    Integer updateBookAndAvailableBalance(@Param("amount")Long amount,@Param("key")String key);

    @Modifying
    @Query("UPDATE Wallet w SET w.bookBalance = w.bookBalance + :amount " +
            "WHERE w.walletKey = :key")
    Integer updateBookBalance(@Param("amount")Long amount,@Param("key")String key);

    @Modifying
    @Query("UPDATE Wallet w SET w.availableBalance = w.availableBalance + :amount " +
            "WHERE w.walletKey = :key")
    Integer updateAvailableBalance(@Param("amount")Long amount,@Param("key")String key);
}
