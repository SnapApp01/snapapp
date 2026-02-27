package com.snappapp.snapng.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${FIREBASE_CONFIG}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() {
        try (FileInputStream serviceAccount =
                     new FileInputStream(firebaseConfigPath)) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("🔥 SNAPAPP: Firebase initialized successfully");
            } else {
                log.info("🔥 SNAPAPP: Firebase already initialized");
            }

        } catch (Exception e) {
            log.error("❌ SNAPAPP: Failed to initialize Firebase", e);
        }
    }
}
