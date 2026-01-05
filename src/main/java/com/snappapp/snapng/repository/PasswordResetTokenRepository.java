package com.snappapp.snapng.repository;

import com.snappapp.snapng.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = ?1 AND t.used = false")
    void invalidateExistingTokens(Long userId);
    
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = ?1 AND t.used = false AND t.expiryDate > CURRENT_TIMESTAMP")
    List<PasswordResetToken> findActiveTokensByUser(UUID userId);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}