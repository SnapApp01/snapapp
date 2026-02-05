package com.snappapp.snapng.snap.admin.services.serviceImpl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.models.approles.Role;
import com.snappapp.snapng.repository.RoleRepository;
import com.snappapp.snapng.snap.admin.services.AdminUserRoleService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminUserRoleServiceImpl implements AdminUserRoleService {

    private final SnapUserRepository snapUserRepository;
    private final RoleRepository roleRepository;

    public AdminUserRoleServiceImpl(SnapUserRepository snapUserRepository,
                                    RoleRepository roleRepository) {
        this.snapUserRepository = snapUserRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    @Override
    public void addRoleToUserByEmail(String email, String roleName) {

        log.info("Admin attempting to add role [{}] to user with email [{}]", roleName, email);

        SnapUser user = snapUserRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> {
                    log.warn("User not found for email={}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Role role = roleRepository
                .findByRoleName(roleName.trim())
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleName);
                    return new ResourceNotFoundException("Role not found");
                });

        boolean alreadyHasRole = user.getRoles()
                .stream()
                .anyMatch(r -> r.getRoleName().equalsIgnoreCase(role.getRoleName()));

        if (alreadyHasRole) {
            log.info("User [{}] already has role [{}]", user.getEmail(), role.getRoleName());
            return; // idempotent
        }

        user.getRoles().add(role);
        snapUserRepository.save(user);

        log.info("Role [{}] successfully added to user [{}]", role.getRoleName(), user.getEmail());
    }
}
