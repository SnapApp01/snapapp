package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.enums.NotificationOwnerType;
import com.snappapp.snapng.snap.data_lib.dtos.AddAppNotificationDto;
import com.snappapp.snapng.snap.data_lib.entities.AppNotification;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.AppNotificationRepository;
import com.snappapp.snapng.snap.data_lib.service.AppNotificationService;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
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
    private final SnapUserService userService;
    private final BusinessService businessService;

    public AppNotificationServiceImpl(AppNotificationRepository repo, SnapUserService userService, BusinessService businessService) {
        this.repo = repo;
        this.userService = userService;
        this.businessService = businessService;
    }

//    @Override
//    public List<AppNotification> get(Long id) {
//        return repo.findByIdAndArchivedFalse(id, PageRequest.of(0,20, Sort.Direction.DESC,"id")).getContent();
//    }

//    @Override
//    public List<AppNotification> get(Long userId) {
//        SnapUser user = userService.findById(userId);
//
//        return repo
//                .findByUidAndArchivedFalse(
//                        user.getIdentifier(),
//                        PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt")
//                )
//                .getContent();
//    }

    @Override
    public List<AppNotification> get(Long userId, NotificationOwnerType ownerType) {

        SnapUser user = userService.findById(userId);

        String uid;

        if (ownerType == NotificationOwnerType.SNAP_BUSINESS) {
            Business business = businessService.getBusinessOfUser(user);

            if (business == null) {
                return List.of(); // user has no business
            }

            uid = business.getIdentifier();
        } else {
            uid = user.getIdentifier();
        }

        if (uid == null) {
            throw new IllegalStateException(
                    "Notification owner has no identifier: " + ownerType
            );
        }

        return repo.findByUidAndReadFalse(
                uid,
                PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt")
        ).getContent();
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
