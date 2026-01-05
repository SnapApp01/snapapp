package com.snappapp.snapng.services.serviceImpl;

import com.snappapp.snapng.services.EmailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {


    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Override
    @Transactional
    public void sendOtp(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            log.info("OTP sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", to, e.getMessage());
        }
    }

    private String encodeToken(String token) {
        return Base64.getUrlEncoder().encodeToString(token.getBytes());
    }
}
