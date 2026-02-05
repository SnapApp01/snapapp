package com.snappapp.snapng.snap.admin.services.serviceImpl;

import com.snappapp.snapng.snap.admin.apimodels.CountPerDayResponse;
import com.snappapp.snapng.snap.admin.apimodels.DashboardSummaryResponse;
import com.snappapp.snapng.snap.admin.services.AdminDashboardService;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestRepository;
import com.snappapp.snapng.snap.data_lib.repositories.SnapUserRepository;
import com.snappapp.snapng.snap.data_lib.repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final SnapUserRepository userRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final VehicleRepository vehicleRepository;

    public AdminDashboardServiceImpl(SnapUserRepository userRepository, DeliveryRequestRepository deliveryRequestRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public DashboardSummaryResponse getSummary() {

        return DashboardSummaryResponse.builder()
                .userCount(userRepository.count())
                .requestCount(deliveryRequestRepository.count())
                .vehicleCount(vehicleRepository.count())
                .build();
    }

    @Override
    public List<CountPerDayResponse> requestDailySummary(int days) {

        LocalDate today = LocalDate.now();

        List<CountPerDayResponse> result = new ArrayList<>();

        for (int i = 0; i < days; i++) {

            LocalDate day = today.minusDays(i);

            long count = deliveryRequestRepository
                    .countByCreatedAtBetween(
                            day.atStartOfDay(),
                            day.plusDays(1).atStartOfDay()
                    );

            result.add(
                    CountPerDayResponse.builder()
                            .date(day)
                            .count(count)
                            .build()
            );
        }

        return result;
    }
}
