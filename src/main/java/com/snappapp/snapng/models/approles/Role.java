package com.snappapp.snapng.models.approles;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@SuperBuilder
public class Role extends BaseEntity {

    @Column(length = 20, nullable = false, unique = true)
    private String roleName; // e.g., "ROLE_USER", "ROLE_ADMIN"

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionItem> permissionItemSet = new HashSet<>();
}