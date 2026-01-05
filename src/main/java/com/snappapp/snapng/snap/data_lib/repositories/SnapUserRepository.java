package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnapUserRepository extends JpaRepository<SnapUser,Long> {
    Optional<SnapUser> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<SnapUser> findByVerificationToken(String token);
    Optional<SnapUser> findByEmailIgnoreCase(String email);
    @Query("SELECT u FROM SnapUser u WHERE u.businesses IS EMPTY AND u.active = true")
    List<SnapUser> findAllUsersNoBusiness();
    Optional<SnapUser> findByEmailAndActiveTrue(String email);
    Optional<SnapUser> findByIdentifierAndActiveTrue(String identifier);
    @Modifying
    @Transactional
    @Query("UPDATE SnapUser u set u.deviceToken = null where u.deviceToken = :token AND u.identifier != :uid")
    void clearUsersWithSameDeviceToken(@Param("token")String token, @Param("uid")String uid);

    Optional<SnapUser> findByIdAndActiveTrue(Long id);
}
