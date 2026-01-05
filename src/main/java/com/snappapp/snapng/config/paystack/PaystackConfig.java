package com.snappapp.snapng.config.paystack;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paystack")
@Data
public class PaystackConfig {
    @Value("${paystack.secret-key}")
    private String secretKey;

    @Value("${paystack.public-key}")
    private String publicKey;

    @Value("${paystack.base-url}")
    private String baseUrl;

    @Value("${paystack.webhook-secret}")
    private String webhookSecret;
}
