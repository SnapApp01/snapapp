package com.snappapp.snapng.models;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dashboardstats")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats extends BaseEntity {
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private double totalPayments;
}