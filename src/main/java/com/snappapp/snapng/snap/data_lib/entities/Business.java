package com.snappapp.snapng.snap.data_lib.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseEntity {
    private String name;
    @Column(unique = true)
    private String code;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "businesses_locations",
            joinColumns = @JoinColumn(name = "business_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "location_id", referencedColumnName = "id")
    )
    @JsonIgnore
    private Set<Location> locations;
    @Column(unique = true, nullable = false)
    private String identifier;
    private Boolean isOnline = false;
    private Boolean isVerified = false;
    private Long balance;
    private String walletKey;
    @Transient
    private Wallet wallet;
    @JsonIgnore
    @ManyToMany(mappedBy = "businesses", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SnapUser> users = new HashSet<>();
}
