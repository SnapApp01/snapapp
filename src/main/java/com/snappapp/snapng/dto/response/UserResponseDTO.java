package com.snappapp.snapng.dto.response;

import com.snappapp.snapng.models.approles.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

        private Long id;
        private String firstname;
        private String lastname;
        private String email;
        private String profilePicture;
        private String phoneNumber;
        private LocalDateTime lastLoginAt;
        private Set<Role> roles = new HashSet<>();
}
