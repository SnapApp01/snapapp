package com.snappapp.snapng.services;

import com.snappapp.snapng.models.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    @Transactional
    int deleteByUserId(Long userId);
}
