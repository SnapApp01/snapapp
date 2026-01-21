package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.BusinessCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BusinessService {
    Business getBusinessOfUser(SnapUser user);
    SnapUser getUserOfBusiness(Business business);
    Business updateOnlineStatus(Business business, boolean online);
    void updateBusinessVerificationStatus(SnapUser user);
    Business createBusiness( BusinessCreationDto dto);
    Business getBusiness(String code);
    Business withWallet(SnapUser user);
    List<Business> getOnlineBusinesses();
}
