package com.snappapp.snapng.services;

import org.springframework.stereotype.Component;

@Component
public interface EmailService {
    void sendOtp(String to, String subject, String text);
}
