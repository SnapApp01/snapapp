package com.snappapp.snapng.config.dataseeder;

import com.snappapp.snapng.models.approles.Role;
import com.snappapp.snapng.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RoleDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(RoleDataSeeder.class);
    private final RoleRepository roleRepository;

    public RoleDataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @EventListener
    @Transactional
    public void seedInitialRoles(ContextRefreshedEvent event) {
        List<String> defaultRoles = List.of("SNAP_USER", "ROLE_ADMIN", "SUPER_ADMIN", "ROLE_DRIVER");

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = Role.builder()
                        .roleName(roleName)
                        .createdAt(LocalDateTime.now())
                        .build();
                roleRepository.save(role);
            }
        }
    }
}