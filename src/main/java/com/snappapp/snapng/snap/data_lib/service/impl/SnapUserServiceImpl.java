package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.exceptions.*;
import com.snappapp.snapng.models.approles.Role;
import com.snappapp.snapng.repository.RoleRepository;
import com.snappapp.snapng.services.VerificationCodeService;
import com.snappapp.snapng.snap.app_service.apimodels.CreateUserDetailRequest;
import com.snappapp.snapng.snap.app_service.apimodels.CreateUserDetailWithBusinessRequest;
import com.snappapp.snapng.snap.data_lib.dtos.BusinessCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import com.snappapp.snapng.utills.TimeBasedUserIdGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SnapUserServiceImpl implements SnapUserService {

    private final SnapUserRepository repo;
    private final WalletService walletService;
    private final VerificationCodeService verificationCodeService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessService businessService;


    public SnapUserServiceImpl(SnapUserRepository repo, WalletService walletService, VerificationCodeService verificationCodeService, RoleRepository roleRepository, PasswordEncoder passwordEncoder, BusinessService businessService) {
        this.repo = repo;
        this.walletService = walletService;
        this.verificationCodeService = verificationCodeService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessService = businessService;
    }

    @Override
    public SnapUser getUserByEmail(String email) {
        return repo.findByEmailAndActiveTrue(email).orElseThrow(()->new ResourceNotFoundException("Email is not registered as a user"));
    }

    @Override
    public SnapUser getUserById(Long id) {
        return repo.findByIdAndActiveTrue(id).orElseThrow(()->new UserNotFoundException("User not registered"));
    }

    @Override
    public boolean checkUserWithIdExists(String id) {
        return repo.findByIdentifierAndActiveTrue(id).isPresent();
    }

    @Override
    @Transactional
    public GenericResponse createUser(CreateUserDetailRequest registerRequest) {
        validateEmail(registerRequest.getEmail(), "User already exists");

        SnapUser user = buildUser(registerRequest);
        repo.save(user);
        verificationCodeService.sendOtpCode(user.getEmail());

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Account created successfully... Check and verify your email")
                .httpStatus(HttpStatus.CREATED)
                .build();
    }


    @Transactional
    @Override
    public GenericResponse createBusinessUser(CreateUserDetailWithBusinessRequest registerRequest) {
        validateEmail(registerRequest.getEmail(), "Driver already exists");

        SnapUser user = buildBusinessUser(registerRequest);

        if (registerRequest.getBusinessName() == null) {
            throw new InvalidParameterException("Business name is empty");
        }
            Business business = businessService.createBusiness(
                    BusinessCreationDto.builder()
                            .companyName(registerRequest.getBusinessName())
                            .build()
            );
            user.getBusinesses().add(business);
            repo.save(user);
            verificationCodeService.sendOtpCode(user.getEmail());

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Account created successfully... Check and verify your email")
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @Override
    public SnapUser updateUser(SnapUser user) {
        return repo.save(user);
    }

    @Transactional
    @Override
    public void addBusinessToUser(SnapUser user, Business business) {
        user.getBusinesses().add(business);
        repo.save(user);
    }

    @Override
    public SnapUser withWallet(Long id) {
        SnapUser user = getUserById(id);
        if(Strings.isNullOrEmpty(user.getWalletKey())){
            Wallet wallet = walletService.save(user.getFirstname()+" "+user.getLastname());
            user.setWalletKey(wallet.getWalletKey());
            repo.save(user);
        }
        user.setWallet(walletService.get(user.getWalletKey()));
        return user;
    }

    @Override
    public SnapUser withDeviceKey(String uid,String deviceKey) {
        repo.clearUsersWithSameDeviceToken(deviceKey,uid);
        SnapUser user = getUserByEmail(uid);
        user.setDeviceToken(deviceKey);
        return repo.save(user);
    }

    @Override
    public List<SnapUser> getUsers() {
        return repo.findAllUsersNoBusiness();
    }

    private SnapUser buildUser(CreateUserDetailRequest request) {
        Role role = roleRepository.findByRoleName("SNAP_USER")
                .orElseThrow(() ->
                        new RoleNotFoundException("SNAP_USER" + " not found in database"));

        return SnapUser.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .balance(0L)
                .identifier(TimeBasedUserIdGenerator.generate())
                .businesses(new HashSet<>())
                .roles(Set.of(role))
                .build();
    }

    private SnapUser buildBusinessUser(CreateUserDetailWithBusinessRequest request) {
        Role role = roleRepository.findByRoleName("ROLE_DRIVER")
                .orElseThrow(() ->
                        new RoleNotFoundException("ROLE_DRIVER" + " not found in database"));

        return SnapUser.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .balance(0L)
                .identifier(TimeBasedUserIdGenerator.generate())
                .businesses(new HashSet<>())
                .roles(Set.of(role))
                .build();
    }


    private void validateEmail(String email, String existsMessage) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidCredentialsException("Provide valid email...");
        }
        if (repo.existsByEmail(email)) {
            throw new UserAlreadyExistsException(existsMessage);
        }
    }

}
