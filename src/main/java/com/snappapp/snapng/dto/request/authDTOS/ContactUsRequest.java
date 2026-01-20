package com.snappapp.snapng.dto.request.authDTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactUsRequest {

    @NotBlank(message = "Name is required!")
    private String name;
    @NotBlank(message = "Message is required!")
    private String message;
    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;
    @NotBlank(message = "Phone number is required!")
    private String phoneNumber;
}