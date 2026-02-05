package com.snappapp.snapng.snap.admin.services.serviceImpl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.admin.apimodels.DeliveryRequestApiResponse;
import com.snappapp.snapng.snap.admin.services.AdminDeliveryRequestService;
import com.snappapp.snapng.snap.data_lib.entities.DeliveryRequest;
import com.snappapp.snapng.snap.data_lib.repositories.DeliveryRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AdminDeliveryRequestServiceImpl implements AdminDeliveryRequestService {

    private final DeliveryRequestRepository deliveryRequestRepository;

    public AdminDeliveryRequestServiceImpl(DeliveryRequestRepository deliveryRequestRepository) {
        this.deliveryRequestRepository = deliveryRequestRepository;
    }

    @Override
    public Page<DeliveryRequestApiResponse> getAll(Integer page, Integer size) {
        Page<DeliveryRequest> requests =
                deliveryRequestRepository.findAll(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                );

        return requests.map(DeliveryRequestApiResponse::new);
    }

    @Override
    public DeliveryRequestApiResponse getById(Long id) {

        Optional<DeliveryRequest> request = deliveryRequestRepository.findById(id);
        if (request.isEmpty()){
                       throw  new ResourceNotFoundException("Request not found");
                }
        DeliveryRequest entity = request.get();
        return new DeliveryRequestApiResponse(entity);
    }
}
