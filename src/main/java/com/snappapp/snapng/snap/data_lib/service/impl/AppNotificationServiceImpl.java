package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import com.snappapp.snapng.snap.data_lib.repositories.AppNotificationRepository;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationRepository repo;

    public AppNotificationServiceImpl(AppNotificationRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<AppNotification> get(Long id) {
        return repo.findByIdAndArchivedFalse(id, PageRequest.of(0,20, Sort.Direction.DESC,"id")).getContent();
    }

    @Override
    public AppNotification getLatest(String uid) {
        return repo.findFirstByUidAndArchivedFalseOrderByIdDesc(uid).orElse(null);
    }

    @Override
    public AppNotification save(AddAppNotificationDto dto) {
        AppNotification notification = new AppNotification();
        notification.setMessage(dto.getMessage());
        notification.setUid(dto.getUid());
        notification.setTitle(dto.getTitle().getTitle());
        notification.setTask(dto.getTask());
        notification.setTaskId(dto.getTaskId());
        notification.setReference(IdUtilities.useDateTimeAtomic()+dto.getUid());
        return repo.save(notification);
    }

    @Override
    public void update(String reference) {
        repo.updateNotificationToRead(reference);
    }

    @Override
    public List<AppNotification> getPending(int size) {
        return repo.findBySentAtIsNullAndAttemptLessThanOrderByAttemptAsc(2,PageRequest.of(0,size)).getContent();
    }

    @Override
    public void updateSentAt(List<Long> ids) {
        repo.updateSentNotifications(LocalDateTime.now(),ids);
    }

    @Override
    public void attempted(AppNotification pn) {
        pn.setAttempt(pn.getAttempt()+1);
        repo.save(pn);
    }
}
