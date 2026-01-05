package com.snappapp.snapng.snap.data_lib.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserCreationDto {
    private String email;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String identifier;
}
