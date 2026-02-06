package com.snappapp.snapng.snap.admin.apimodels;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserAdminResponse {

    private Long id;
    private String identifier;
    private String firstname;
    private String lastname;
    private String email;
    private boolean enabled;
    private String phoneNumber;
    private boolean emailVerified;


    // null if user has no business
    private String businessName;

    public UserAdminResponse(SnapUser user) {
        this.id = user.getId();
        this.identifier = user.getIdentifier();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.phoneNumber = user.getPhoneNumber();
        this.emailVerified = user.isEmailVerified();

        if (user.getBusinesses() != null && !user.getBusinesses().isEmpty()) {
            // pick first business
            this.businessName = user.getBusinesses()
                    .stream()
                    .findFirst()
                    .map(Business::getName)
                    .orElse(null);
        }
    }
}
