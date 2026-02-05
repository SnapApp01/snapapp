package com.snappapp.snapng.snap.admin.services;

import com.snappapp.snapng.snap.admin.apimodels.DeliveryRequestApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public interface AdminDeliveryRequestService {
    Page<DeliveryRequestApiResponse> getAll(Integer page, Integer size);

    DeliveryRequestApiResponse getById(Long id);
}
