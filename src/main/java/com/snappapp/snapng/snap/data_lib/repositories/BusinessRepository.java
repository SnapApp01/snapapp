package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business,Long> {
    Optional<Business> findByCodeAndActiveTrue(String code);
    List<Business> findByActiveTrueAndIsOnlineTrueAndIsVerifiedTrue();
}
