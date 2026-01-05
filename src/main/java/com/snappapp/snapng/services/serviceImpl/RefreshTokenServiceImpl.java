package com.snappapp.snapng.services.serviceImpl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.exceptions.TokenRefreshException;
import com.snappapp.snapng.models.RefreshToken;
import com.snappapp.snapng.repository.RefreshTokenRepository;
import com.snappapp.snapng.services.RefreshTokenService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SnapUserRepository userRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, SnapUserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            // 🔹 Update existing refresh token (invalidate old one)
            RefreshToken refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(refreshToken);
        } else {
            // 🔹 Create new refresh token if none exists
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found")));
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(refreshToken);
        }
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired. Please login again");
        }
        return token;
    }
    @Transactional
    @Override
    public int deleteByUserId(Long userId) {
        SnapUser user = userRepository.findById(userId).orElseThrow();
        return refreshTokenRepository.deleteByUser(user);
    }
}
