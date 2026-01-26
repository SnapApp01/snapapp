package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    Page<AppNotification> findByUidAndArchivedFalse(String uid, Pageable pageable);
    Optional<AppNotification> findFirstByUidAndArchivedFalseOrderByIdDesc(String uid);
    @Query("UPDATE AppNotification n SET n.read = true WHERE n.reference = :ref")
    @Modifying
    @Transactional
    void updateNotificationToRead(@Param("ref")String ref);

    Page<AppNotification> findBySentAtIsNullAndAttemptLessThanOrderByAttemptAsc(int max,Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE AppNotification p SET p.sentAt = :sentAt WHERE p.id IN :ids")
    int updateSentNotifications(LocalDateTime sentAt, List<Long> ids);

    Page<AppNotification> findByIdAndArchivedFalse(Long id, Pageable pageable);
}
