package com.snappapp.snapng.services.serviceImpl;


import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.MediaUploadRequest;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.exceptions.UserNotFoundException;
import com.snappapp.snapng.models.Media;
import com.snappapp.snapng.services.MediaService;
import com.snappapp.snapng.services.UserService;
import com.snappapp.snapng.snap.admin.apimodels.UserAdminResponse;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.utills.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final SnapUserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final MediaService mediaService;
    private final BusinessService businessService;

    public UserServiceImpl(SnapUserRepository userRepository, SecurityUtil securityUtil, MediaService mediaService, BusinessService businessService) {
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.mediaService = mediaService;
        this.businessService = businessService;
    }


    @Override
    public GenericResponse getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<SnapUser> users = userRepository.findAll(pageable);

        Page<UserAdminResponse> result =
                users.map(UserAdminResponse::new);

        return new GenericResponse("Users fetched successfully", HttpStatus.OK, result);
    }

    @Override
    public GenericResponse getUserById(Long id) {
        SnapUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new GenericResponse("User found", HttpStatus.OK, user);
    }

    @Transactional
    @Override
    public GenericResponse changeUserBusinessStatus(Long id) {
        SnapUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        businessService.updateBusinessVerificationStatus(user);
        return GenericResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("Business verification status verified successfully...")
                .build();
    }

    @Override
    public GenericResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        SnapUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstname(userUpdateRequest.getFirstname());
        user.setLastname(userUpdateRequest.getLastname());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new GenericResponse("User updated successfully", HttpStatus.OK, user);
    }

    @Override
    public GenericResponse toggleDisableUser(Long id) {
        SnapUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        user.setUpdatedBy(securityUtil.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new GenericResponse("User disabled successfully", HttpStatus.OK);
    }

    @Override
    public GenericResponse deleteUser(Long id) {
        SnapUser user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        return new GenericResponse("User deleted successfully", HttpStatus.OK, null);
    }

    @Override
    @Transactional
    public GenericResponse uploadUserProfilePicture(Long userId, MultipartFile file, String description, String mediaType) {

        SnapUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found..."));
            try {
                MediaUploadRequest mediaUploadRequest = new MediaUploadRequest();
                mediaUploadRequest.setDescription(description);
                mediaUploadRequest.setFile(file);
                mediaUploadRequest.setMediaType(mediaType);
                Media media = mediaService.uploadImage(mediaUploadRequest);

                user.setProfilePicture(media.getUrl());
                userRepository.save(user);

                return GenericResponse.builder()
                        .isSuccess(true)
                        .httpStatus(HttpStatus.OK)
                        .message("User profile picture uploaded successfully")
                        .build();
            } catch (Exception e) {
                log.error("Error uploading profile picture: {}", e.getMessage(), e);
                throw new FailedProcessException("Failed to upload profile picture: " + e.getMessage());
            }
        }

}
