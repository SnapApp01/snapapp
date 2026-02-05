package com.snappapp.snapng.snap.admin.apimodels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRoleByEmailRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String roleName;
}
