package com.snappapp.snapng.repository;


import com.snappapp.snapng.models.RefreshToken;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(SnapUser user);

    Optional<RefreshToken> findByUserId(Long userId);
}
