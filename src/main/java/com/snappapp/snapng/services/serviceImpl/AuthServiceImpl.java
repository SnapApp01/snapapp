package com.snappapp.snapng.services.serviceImpl;

import com.snappapp.snapng.config.security.UserDetailsImpl;
import com.snappapp.snapng.config.security.jwt.JwtUtils;
import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.*;
import com.snappapp.snapng.dto.response.LoginResponseDto;
import com.snappapp.snapng.dto.token.RefreshTokenRequest;
import com.snappapp.snapng.dto.token.TokenRefreshResponse;
import com.snappapp.snapng.exceptions.*;
import com.snappapp.snapng.models.RefreshToken;
import com.snappapp.snapng.models.approles.Role;
import com.snappapp.snapng.repository.RoleRepository;
import com.snappapp.snapng.services.AuthService;
import com.snappapp.snapng.services.RefreshTokenService;
import com.snappapp.snapng.services.VerificationCodeService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.utills.SecurityUtil;
import com.snappapp.snapng.utills.TimeBasedUserIdGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SnapUserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SecurityUtil securityUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, SnapUserRepository userRepository, VerificationCodeService verificationCodeService, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, SecurityUtil securityUtil, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.verificationCodeService = verificationCodeService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.securityUtil = securityUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public GenericResponse login(LoginRequest loginRequestDto) {
        if (loginRequestDto.getEmail() == null || loginRequestDto.getPassword() == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 🔹 Find SnapUser by email
        Optional<SnapUser> optionalUser = userRepository.findByEmail(loginRequestDto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        SnapUser userEntity = optionalUser.get();

        // 🔹 Validate password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), userEntity.getPassword())) {
            userEntity.incrementFailedLoginAttempts();
            userRepository.save(userEntity);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // 🔹 Authenticate user with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 🔹 Generate tokens
        String accessToken = jwtUtils.generateJwtToken(authentication);
//        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getEmail());

        RefreshToken savedRefreshToken = refreshTokenService.createRefreshToken(userEntity.getId());

        // 🔹 Collect user roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 🔹 Update last login time
        userEntity.setLastLoginAt(LocalDateTime.now());
        userRepository.save(userEntity);

        // 🔹 Build login response
        LoginResponseDto responseDto = new LoginResponseDto(
                userDetails.getId(),
                userDetails.getFirstname(),
                userDetails.getLastname(),
                userDetails.getEmail(),
                userDetails.isEnabled(),
                userDetails.getUsername(),
                "Bearer",       // tokenType
                accessToken,    // short-lived JWT
                savedRefreshToken.getToken(),   // long-lived refresh token
                roles
        );

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Sign in successfully!")
                .data(responseDto)
                .httpStatus(HttpStatus.OK)
                .build();
    }


    @Override
    public GenericResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());

                    TokenRefreshResponse tokenResponse = new TokenRefreshResponse(newAccessToken, requestRefreshToken);

                    return GenericResponse.builder()
                            .isSuccess(true)
                            .message("Token refreshed successfully")
                            .data(tokenResponse)
                            .httpStatus(HttpStatus.OK)
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not valid!"));
    }

    @Override
    public GenericResponse verifyEmail(String email, String code) {
        SnapUser user = userRepository.findByEmail(email).orElseThrow( () -> new UserNotFoundException("User not found for provided email:" + email));
        if (user.isEmailVerified()) {
            throw new UserAlreadyExistsException("User already verified");
        }
        boolean emailVerified = verificationCodeService.verifyOtpCode(email, code);
        if (!emailVerified) {
            throw new InvalidTokenException("Email verification failed...");
        }
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.save(user);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Email verified successfully.")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse changePassword(ChangePasswordRequest request) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw  new InvalidCredentialsException("Incorrect credential provided.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Password changed successfully.")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse deleteAccount(Long userId) {
        Optional<SnapUser> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()){
            throw  new InvalidCredentialsException("User not found with provided id.");
        }
        userRepository.deleteById(userId);
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Account deleted successfully.")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private SnapUser createUserTemplate(RegisterRequest signUpRequestDto) {
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