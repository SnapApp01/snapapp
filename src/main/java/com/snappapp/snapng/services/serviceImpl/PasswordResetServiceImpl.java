//package com.snappapp.snapng.services.serviceImpl;
//
//import com.snappapp.snapng.dto.GenericResponse;
//import com.snappapp.snapng.dto.response.CompleteResetRequest;
//import com.snappapp.snapng.exceptions.InvalidRequestException;
//import com.snappapp.snapng.exceptions.InvalidTokenException;
//import com.snappapp.snapng.models.PasswordResetToken;
//import com.snappapp.snapng.repository.PasswordResetTokenRepository;
//import com.snappapp.snapng.services.EmailService;
//import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
//import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.Base64;
//
//@Service
//@Transactional
//@Slf4j
//public class PasswordResetServiceImpl implements PasswordResetService {
//
//    private final SnapUserRepository userRepository;
//    private final PasswordResetTokenRepository tokenRepository;
//    private final EmailService emailService;
//    private final PasswordEncoder passwordEncoder;
//
//    public PasswordResetServiceImpl(SnapUserRepository userRepository, PasswordResetTokenRepository tokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.tokenRepository = tokenRepository;
//        this.emailService = emailService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
////
////    @Override
////    @Transactional
////    public GenericResponse initiatePasswordReset(InitiatePasswordReset resetPasswordRequest) {
////        SnapUser user = userRepository.findByEmailIgnoreCase(resetPasswordRequest.getEmail())
////                .orElseThrow(() -> new UserNotFoundException("User with this email does not exist"));
////
////        // Invalidate previous tokens for this user
////        tokenRepository.invalidateExistingTokens(user.getId());
////
////        // Generate a fresh token
////        String token = UUID.randomUUID().toString();
////        PasswordResetToken resetToken = PasswordResetToken.builder()
////                .token(token) // store raw token in DB
////                .user(user)
////                .createdAt(LocalDateTime.now())
////                .updatedAt(LocalDateTime.now())
////                .expiryDate(Instant.now().plus(15, ChronoUnit.MINUTES))
////                .used(false)
////                .build();
////
////        tokenRepository.save(resetToken);
////
////        // Encode only for email link
////        emailService.sendPasswordResetEmail(
////                user.getEmail(),
////                user.getFirstname(),
////                encodeToken(token)
////        );
////
////        return GenericResponse.builder()
////                .isSuccess(true)
////                .httpStatus(HttpStatus.OK)
////                .message("If an account exists with this email, a reset link has been sent")
////                .build();
////    }
//
//    @Override
//    public GenericResponse validateResetToken(String encodedToken) {
//        String token = decodeToken(encodedToken); // decode before lookup
//
//        PasswordResetToken resetToken = tokenRepository.findByToken(token)
//                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));
//
//        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
//            throw new InvalidTokenException("Token has expired");
//        }
//
//        if (resetToken.isUsed()) {
//            throw new InvalidTokenException("Token has already been used");
//        }
//
//        return GenericResponse.builder()
//                .isSuccess(true)
//                .message("Token is valid")
//                .httpStatus(HttpStatus.OK)
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public GenericResponse completePasswordReset(CompleteResetRequest request) {
//        // Decode before lookup
//        String token = decodeToken(request.getToken());
//
//        PasswordResetToken resetToken = tokenRepository.findByToken(token)
//                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));
//
//        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
//            throw new InvalidTokenException("Token has expired");
//        }
//
//        if (resetToken.isUsed()) {
//            throw new InvalidTokenException("Token has already been used");
//        }
//
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            throw new InvalidRequestException("Passwords do not match");
//        }
//
//        // Update password
//        SnapUser user = resetToken.getUser();
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        user.setPasswordChangedDate(Instant.now());
//        userRepository.save(user);
//
//        // Mark token as used
//        resetToken.setUsed(true);
//        tokenRepository.save(resetToken);
//
//        return GenericResponse.builder()
//                .isSuccess(true)
//                .message("Password has been reset successfully")
//                .httpStatus(HttpStatus.OK)
//                .build();
//    }
//
//    @Scheduled(cron = "0 0 3 * * ?") // Runs daily at 3 AM
//    public void cleanupExpiredTokens() {
//        tokenRepository.deleteExpiredTokens();
//    }
//
//    private String encodeToken(String token) {
//        return Base64.getUrlEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
//    }
//
//    private String decodeToken(String encodedToken) {
//        return new String(Base64.getUrlDecoder().decode(encodedToken), StandardCharsets.UTF_8);
//    }
//}