package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.snap.app_service.apimodels.CreatePriceProposalRequest;
import com.snappapp.snapng.snap.app_service.apimodels.DeliveryPriceProposalResponse;
import com.snappapp.snapng.snap.app_service.apimodels.UpdatePriceProposalRequest;
import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/price-proposal")
@RestController
public class PriceManagementController {

    private final PriceManagementService service;
    private final SecurityUtil securityUtil;

    public PriceManagementController(PriceManagementService service, SecurityUtil securityUtil) {
        this.service = service;

        this.securityUtil = securityUtil;
    }

    @GetMapping("/{trackId}")
    public List<DeliveryPriceProposalResponse> getProposals(
            @PathVariable("trackId")String trackId){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getPriceProposals(trackId, user.getId());
    }

    @GetMapping("/business/{trackId}")
    public DeliveryPriceProposalResponse getProposal(
            @PathVariable("trackId")String trackId){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getPriceProposalByBusiness(trackId, user.getId());
    }

    @PostMapping
    public DeliveryPriceProposalResponse addProposal(@Validated @RequestBody CreatePriceProposalRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.addPriceProposal(user.getId(), request);
    }

    @PutMapping
    public DeliveryPriceProposalResponse updateProposal(@Validated @RequestBody UpdatePriceProposalRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        DeliveryPriceProposalResponse response = service.updateProposal(user.getId(), request);
        service.sendNotificationToOtherProposals(request, user.getId());
        return response;
    }
}
