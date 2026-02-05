package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.DeliveryRequestApiResponse;
import com.snappapp.snapng.snap.admin.services.AdminDeliveryRequestService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/admin/requests")
@RestController
public class AdminDeliveryRequestController {

    private final AdminDeliveryRequestService service;

    public AdminDeliveryRequestController(AdminDeliveryRequestService service) {
        this.service = service;
    }


    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<DeliveryRequestApiResponse> getTransactions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return service.getAll(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DeliveryRequestApiResponse getUser(@PathVariable Long id) {
        return service.getById(id);
    }
}
