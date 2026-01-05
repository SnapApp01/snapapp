package com.snappapp.snapng.models;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verificationCode")
@SuperBuilder
@Entity
public class VerificationCode extends BaseEntity {


    private String email;
    private String code;

    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
