package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginApiResponse {
    private String fullname;
    private String token;
}
