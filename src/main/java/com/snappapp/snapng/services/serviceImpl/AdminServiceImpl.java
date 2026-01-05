package com.snappapp.snapng.services.serviceImpl;

import com.snappapp.snapng.repository.RoleRepository;

import com.snappapp.snapng.services.AdminService;
import com.snappapp.snapng.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public AdminServiceImpl(RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
}
