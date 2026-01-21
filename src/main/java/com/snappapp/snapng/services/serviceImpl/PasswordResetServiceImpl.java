package com.snappapp.snapng.services.serviceImpl;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.InitiatePasswordReset;
import com.snappapp.snapng.dto.response.CompleteResetRequest;
import com.snappapp.snapng.exceptions.InvalidRequestException;
import com.snappapp.snapng.exceptions.InvalidTokenException;
import com.snappapp.snapng.exceptions.UserNotFoundException;
import com.snappapp.snapng.models.PasswordResetToken;
import com.snappapp.snapng.repository.PasswordResetTokenRepository;
import com.snappapp.snapng.services.EmailService;
import com.snappapp.snapng.services.PasswordResetService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.utills.OtpUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final SnapUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(SnapUserRepository userRepository, PasswordResetTokenRepository tokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public GenericResponse initiatePasswordReset(InitiatePasswordReset resetPasswordRequest) {
        Optional<SnapUser> user = userRepository.findByEmailIgnoreCase(resetPasswordRequest.getEmail());
        if (user.isEmpty()) {
            return GenericResponse.builder()
                    .isSuccess(true)
                    .httpStatus(HttpStatus.OK)
                    .message("User with this email does not exist")
                    .build();
        }
        // Invalidate previous tokens for this user
        tokenRepository.invalidateExistingTokens(user.get().getId());

        // Generate a fresh token
        String token = OtpUtil.generateSixDigitCode();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token) // store raw token in DB
                .user(user.get())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiryDate(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Encode only for email link
        emailService.sendOtp(
                user.get().getEmail(),
                "Reset Password",
                token
        );

        return GenericResponse.builder()
                .isSuccess(true)
                .httpStatus(HttpStatus.OK)
                .message("A reset token has been sent")
                .build();
    }

    @Override
    public GenericResponse validateResetToken(String token) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token has expired");
        }

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Token is valid")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse completePasswordReset(CompleteResetRequest request) {
        String token = request.getToken();

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token has expired");
        }

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        // Update password
        SnapUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedDate(Instant.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Password has been reset successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Scheduled(cron = "0 0 3 * * ?") // Runs daily at 3 AM
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }
}