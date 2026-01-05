package com.snappapp.snapng.utills;

import com.snappapp.snapng.config.security.UserDetailsImpl;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for handling Spring Security authentication operations.
 * Provides methods to retrieve current authenticated user information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final SnapUserRepository userRepository;

    /**
     * Retrieves the currently authenticated user from the security context.
     * 
     * @return User entity of the currently authenticated user
     * @throws RuntimeException if no authenticated user is found
     * @throws UsernameNotFoundException if user exists in security context but not in database
     */
    public SnapUser getCurrentLoggedInUser() {
        log.debug("Attempting to retrieve current logged-in user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetailsImpl) {

            String email = ((UserDetailsImpl) authentication.getPrincipal()).getEmail();
            log.debug("Found authenticated user with email: {}", email);
            
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User with email {} not found in database", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });
        }

        log.warn("No authenticated user found in security context");
        throw new RuntimeException("No authenticated user found");
    }

    /**
     * Safely retrieves the currently authenticated user from the security context.
     * Returns Optional.empty() if no user is authenticated instead of throwing exception.
     * 
     * @return Optional containing User entity if authenticated, empty otherwise
     */
    public Optional<SnapUser> getCurrentLoggedInUserSafely() {
        try {
            return Optional.of(getCurrentLoggedInUser());
        } catch (Exception e) {
            log.debug("No authenticated user found: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the email of the currently authenticated user.
     * 
     * @return Email of the currently authenticated user
     * @throws RuntimeException if no authenticated user is found
     */
    public String getCurrentUserEmail() {
        log.debug("Retrieving current user email");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetailsImpl) {
            
            String email = ((UserDetailsImpl) authentication.getPrincipal()).getEmail();
            log.debug("Current user email: {}", email);
            return email;
        }

        log.warn("No authenticated user found when retrieving email");
        throw new RuntimeException("No authenticated user found");
    }

    /**
     * Safely retrieves the email of the currently authenticated user.
     * Returns Optional.empty() if no user is authenticated.
     * 
     * @return Optional containing email if authenticated, empty otherwise
     */
    public Optional<String> getCurrentUserEmailSafely() {
        try {
            return Optional.of(getCurrentUserEmail());
        } catch (Exception e) {
            log.debug("No authenticated user email found: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the ID of the currently authenticated user.
     * 
     * @return ID of the currently authenticated user
     * @throws RuntimeException if no authenticated user is found
     * @throws UsernameNotFoundException if user exists in security context but not in database
     */
    public Long getCurrentUserId() {
        return getCurrentLoggedInUser().getId();
    }

    /**
     * Safely retrieves the ID of the currently authenticated user.
     * Returns Optional.empty() if no user is authenticated.
     * 
     * @return Optional containing user ID if authenticated, empty otherwise
     */
    public Optional<Long> getCurrentUserIdSafely() {
        return getCurrentLoggedInUserSafely().map(SnapUser::getId);
    }

    /**
     * Checks if there is currently an authenticated user.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        boolean isAuthenticated = authentication != null 
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetailsImpl;
        
        log.debug("User authentication status: {}", isAuthenticated);
        return isAuthenticated;
    }

    /**
     * Checks if the currently authenticated user matches the given email.
     * 
     * @param email Email to check against current user
     * @return true if current user's email matches the given email, false otherwise
     */
    public boolean isCurrentUser(String email) {
        return getCurrentUserEmailSafely()
                .map(currentEmail -> currentEmail.equals(email))
                .orElse(false);
    }

    /**
     * Checks if the currently authenticated user matches the given user ID.
     * 
     * @param userId User ID to check against current user
     * @return true if current user's ID matches the given ID, false otherwise
     */
    public boolean isCurrentUser(Long userId) {
        return getCurrentUserIdSafely()
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }
}