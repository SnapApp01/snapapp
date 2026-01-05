package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.snap.app_service.apimodels.CalculateMinimumCostResponse;
import com.snappapp.snapng.snap.app_service.apimodels.CreateDeliveryRequest;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryRequestCreationResponse;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryRequestRetrievalResponse;
import com.snappapp.snapng.snap.app_service.services.DeliveryRequestManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/delivery-request")
public class DeliveryRequestController {

    private final DeliveryRequestManagementService service;
    private final SecurityUtil securityUtil;


    public DeliveryRequestController(DeliveryRequestManagementService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/create")
    public DeliveryRequestCreationResponse create(@Validated @RequestBody CreateDeliveryRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        DeliveryRequestCreationResponse rsp = service.create(user.getId(), request);
        service.pushNotificationForNewRequest(rsp.getTrackId());
        return rsp;
    }

    @PutMapping("/calculate")
    public CalculateMinimumCostResponse calculateMinimumCost(@Validated @RequestBody CreateDeliveryRequest request){
        return service.checkMinimumCost(request);
    }

    @GetMapping("/has-pending")
    public Boolean hasPending(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.hasPendingRequest(user.getId());
    }

    @GetMapping("/pending")
    public List<DeliveryRequestRetrievalResponse> getPendingForUser(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getPendingRequest(user.getId());
    }

    @GetMapping
    public List<DeliveryRequestRetrievalResponse> getForUser(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getUserRequests(user.getId());
    }

    @GetMapping("/track/{trackingId}")
    public DeliveryRequestRetrievalResponse trackDeliveryRequest(@PathVariable(name = "trackingId")String trackingId){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getUserRequest(user.getId(), trackingId);
    }

    @GetMapping("/business/track/{trackingId}")
    public DeliveryRequestRetrievalResponse trackDeliveryRequestByBusiness(@PathVariable(name = "trackingId")String trackingId)  {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getRequestByBusiness(user.getId(), trackingId);
    }

    @PutMapping("/business/update-status/{trackingId}")
    public DeliveryRequestRetrievalResponse updateStatusDeliveryRequestByBusiness(@PathVariable(name = "trackingId")String trackingId)  {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.updateRequestStatusByBusiness(user.getId(), trackingId);
    }

    @PutMapping("/complete/{trackingId}")
    public DeliveryRequestRetrievalResponse completeRequestByUser(@PathVariable(name = "trackingId")String trackingId){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.completeDelivery(user.getId(), trackingId);
    }

    @PutMapping("/cancel/{trackingId}")
    public DeliveryRequestRetrievalResponse cancelRequestByUser(@PathVariable(name = "trackingId")String trackingId){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.cancelDelivery(user.getId(), trackingId);
    }

    @GetMapping("/by-business")
    public List<DeliveryRequestRetrievalResponse> getForBusiness(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getAcceptedRequests(user.getId());
    }

    @GetMapping("/pending/instant")
    public List<DeliveryRequestRetrievalResponse> getPendingForBusiness() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getUnAssignedRequests(user.getId());
    }

    @GetMapping("/pending/future")
    public List<DeliveryRequestRetrievalResponse> getPendingFutureForBusiness(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getUnAssignedNotInstantRequests(user.getId());
    }
}
