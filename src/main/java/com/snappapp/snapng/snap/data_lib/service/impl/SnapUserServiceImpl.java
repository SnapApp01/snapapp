package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.exceptions.*;
import com.snappapp.snapng.models.approles.Role;
import com.snappapp.snapng.repository.RoleRepository;
import com.snappapp.snapng.services.VerificationCodeService;
import com.snappapp.snapng.snap.app_service.apimodels.CreateUserDetailRequest;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import com.snappapp.snapng.utills.TimeBasedUserIdGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public SnapUserServiceImpl(SnapUserRepository repo, WalletService walletService, VerificationCodeService verificationCodeService, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.walletService = walletService;
        this.verificationCodeService = verificationCodeService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
//
//    @Override
//    public SnapUser createUser(UserCreationDto dto) {
//        SnapUser user = new SnapUser();
//        user.setBalance(0L);
//        user.setBusinesses(new HashSet<>());
//        user.setEmail(dto.getEmail().trim());
//        user.setFirstname(dto.getFirstname().trim());
//        user.setLastname(dto.getLastname().trim());
//        user.setIdentifier(dto.getIdentifier());
//        user.setPhoneNumber(dto.getPhoneNumber());
//        return repo.save(user);
//    }

    @Override
    @Transactional
    public SnapUser createUser(CreateUserDetailRequest registerRequest) {
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            throw new InvalidCredentialsException("Provide valid email...");
        }
        if (repo.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }
        SnapUser user = createUserTemplate(registerRequest);
        SnapUser savedUser = repo.save(user);
        verificationCodeService.sendOtpCode(user.getEmail());

        return savedUser;
    }

    @Override
    public SnapUser updateUser(SnapUser user) {
        return repo.save(user);
    }

    @Override
    public SnapUser addBusinessToUser(SnapUser user, Business business) {
        user.getBusinesses().add(business);
        return repo.save(user);
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

    private SnapUser createUserTemplate(CreateUserDetailRequest signUpRequestDto) {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByRoleName("SNAP_USER")
                .orElseThrow(() -> new RoleNotFoundException("SNAP_USER not found in database"));
        roles.add(role);
        return SnapUser.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .firstname(signUpRequestDto.getFirstname())
                .lastname(signUpRequestDto.getLastname())
                .phoneNumber(signUpRequestDto.getPhoneNumber())
                .balance(0L)
                .businesses(new HashSet<>())
                .identifier(TimeBasedUserIdGenerator.generate())
                .roles(roles)
                .email(signUpRequestDto.getEmail())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .build();
    }
}
