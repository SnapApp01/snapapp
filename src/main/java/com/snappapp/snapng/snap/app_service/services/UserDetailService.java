package com.snappapp.snapng.snap.app_service.services;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.RegisterRequest;
import com.snappapp.snapng.exceptions.*;
import com.snappapp.snapng.snap.app_service.apimodels.*;
import com.snappapp.snapng.snap.data_lib.dtos.BusinessCreationDto;
import com.snappapp.snapng.snap.data_lib.dtos.UserCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailService {

    private final SnapUserService userService;
    private final BusinessService businessService;

    public UserDetailService(SnapUserService userService, BusinessService businessService) {
        this.userService = userService;
        this.businessService = businessService;
    }

    public UserDetailResponse getUser(Long userId){
        SnapUser user = userService.withWallet(userId);
        Business business = businessService.withWallet(user);
        return UserDetailResponse
                .builder()
                .id(user.getIdentifier())
                .lastname(user.getLastname())
                .phone(user.getPhoneNumber())
                .business(business==null?null: BusinessDetailResponse
                        .builder()
                        .company(business.getName())
                        .online(business.getIsOnline())
                        .verified(business.getIsVerified())
                        .balanceInLong(business.getWallet().getAvailableBalance())
                        .bookBalanceInLong(business.getWallet().getBookBalance())
                        .build())
                .balanceInLong(user.getWallet().getAvailableBalance())
                .bookBalanceInLong(user.getWallet().getBookBalance())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .build();
    }

    public void updateUser(UpdateUserDetailRequest request, Long userId){
        SnapUser user = userService.getUserById(userId);
        if(!Strings.isNullOrEmpty(request.getFirstname())){
            user.setFirstname(request.getFirstname());
        }
        if(!Strings.isNullOrEmpty(request.getLastname())){
            user.setLastname(request.getLastname());
        }
        if(!Strings.isNullOrEmpty(request.getPhone())){
            user.setPhoneNumber(request.getPhone());
        }
        userService.updateUser(user);
    }

    public void addBusiness(AddBusinessRequest request, Long userId) {
        SnapUser user = userService.getUserById(userId);
        if(businessService.getBusinessOfUser(user)!=null){
            throw new ResourceAlreadyExistsException("Business already exists for user");
        }
        addBusiness(request, user);
    }

    public void updateBusinessStatus(Boolean status,Long userId) {
        SnapUser user = userService.getUserById(userId);

        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new ResourceNotFoundException("Business does not exist for this user");
        }
        if(!business.getIsVerified() && status){
            throw new FailedProcessException("Business is not yet verified");
        }
        businessService.updateOnlineStatus(business,status);
    }

    private void addBusiness(AddBusinessRequest request, SnapUser user){
        Business business = businessService.createBusiness(BusinessCreationDto.builder()
                .companyName(request.getCompanyName().trim())
                .build());
        userService.addBusinessToUser(user,business);
    }

}
