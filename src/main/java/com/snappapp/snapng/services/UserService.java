package com.snappapp.snapng.services;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.snap.data_lib.dtos.UserCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface UserService {

    GenericResponse getAllUsers(int page, int size);

    GenericResponse getUserById(Long id);

    GenericResponse updateUser(Long id, UserUpdateRequest userUpdateRequest);

    GenericResponse toggleDisableUser(Long id);

    GenericResponse deleteUser(Long id);

    GenericResponse uploadUserProfilePicture(Long userId, MultipartFile file, String description, String mediaType);

    GenericResponse changeUserBusinessStatus(Long id);

//    SnapUser getUserByEmail(String email);
//    SnapUser getUserById(String id);
//    boolean checkUserWithIdExists(String id);
//    SnapUser createUser(UserCreationDto dto);
//    SnapUser updateUser(SnapUser user);
//    SnapUser addBusinessToUser(SnapUser user, Business business);
//    SnapUser withWallet(String id);
//    SnapUser withDeviceKey(String uid,String deviceKey);
//    List<SnapUser> getUsers();
}
