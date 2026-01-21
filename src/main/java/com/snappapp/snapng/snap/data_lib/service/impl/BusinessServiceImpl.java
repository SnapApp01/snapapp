package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.BusinessCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.data_lib.repositories.BusinessRepository;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import com.snappapp.snapng.snap.utils.utilities.StringUtilities;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository repo;
    private final WalletService walletService;

    public BusinessServiceImpl(BusinessRepository repo, WalletService walletService) {
        this.repo = repo;
        this.walletService = walletService;
    }

    @Override
    public Business getBusinessOfUser(SnapUser user) {
        if(user.getBusinesses().isEmpty()){
            return null;
        }
        Business business = user.getBusinesses().iterator().next();
        return business;
    }

    public SnapUser getUserOfBusiness(Business business){
        if(business.getUsers().isEmpty()){
            return null;
        }
        return business.getUsers().iterator().next();
    }

    @Override
    public Business updateOnlineStatus(Business business, boolean online) {
        business.setIsOnline(online);
        return repo.save(business);
    }

    @Override
    public void updateBusinessVerificationStatus(SnapUser user) {
        Optional<Business> business = repo.findByUsersContaining(user);
        if(business.isEmpty()){
            throw new ResourceNotFoundException("Business not found for this user");
        }
        Business business1 = business.get();
        business1.setIsVerified(true);
        repo.save(business1);
    }

    @Transactional
    @Override
    public Business createBusiness(BusinessCreationDto dto) {
        Business business = new Business();
        business.setIsOnline(false);
        business.setName(StringUtilities.trim(dto.getCompanyName()));
        business.setCode(IdUtilities.shortUUID());
        business.setBalance(0L);
        return repo.save(business);
    }

    @Override
    public Business getBusiness(String code) {
        return repo.findByCodeAndActiveTrue(code).orElseThrow(()->new ResourceNotFoundException("Business with code not found"));
    }

    @Override
    public Business withWallet(SnapUser user) {
        Business business = getBusinessOfUser(user);
        if(business==null){
            return null;
        }
        if(Strings.isNullOrEmpty(business.getWalletKey())){
            Wallet wallet = walletService.save(business.getName().toUpperCase());
            business.setWalletKey(wallet.getWalletKey());
            repo.save(business);
            business.setWallet(wallet);
        }
        else{
            business.setWallet(walletService.get(business.getWalletKey()));
        }
        return business;
    }

    @Override
    public List<Business> getOnlineBusinesses() {
        return repo.findByActiveTrueAndIsOnlineTrueAndIsVerifiedTrue();
    }
}
