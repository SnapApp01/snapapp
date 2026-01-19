package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.app_service.apimodels.CreateUserDetailRequest;
import com.snappapp.snapng.snap.app_service.apimodels.CreateUserDetailWithBusinessRequest;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import jakarta.transaction.Transactional;

import java.util.List;

public interface SnapUserService {
    SnapUser getUserByEmail(String email);
//    SnapUser getUserById(String id);

    SnapUser getUserById(Long id);

    boolean checkUserWithIdExists(String id);
    SnapUser createUser(CreateUserDetailRequest dto);

    SnapUser createBusinessUser(CreateUserDetailWithBusinessRequest registerRequest);

    SnapUser updateUser(SnapUser user);
    SnapUser addBusinessToUser(SnapUser user, Business business);
    SnapUser withWallet(Long id);
    SnapUser withDeviceKey(String uid,String deviceKey);
    List<SnapUser> getUsers();
}
