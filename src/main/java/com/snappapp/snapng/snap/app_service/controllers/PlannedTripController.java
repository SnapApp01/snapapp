package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.snap.app_service.apimodels.*;
import com.snappapp.snapng.snap.app_service.services.TripPlanManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/planned-trip")
@RestController
public class PlannedTripController {
    private final TripPlanManagementService service;
    private final SecurityUtil securityUtil;

    public PlannedTripController(TripPlanManagementService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public PlannedTripResponse create(@Validated @RequestBody CreatePlannedTripRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        PlannedTripResponse rsp =  service.create(user.getId(), request);
        service.notifyOnPlannedTrip(rsp.getReference());
        return rsp;
    }

    @GetMapping("/one/{reference}")
    public PlannedTripResponse getOne(@PathVariable("reference")String ref){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getTrip(ref, user.getId());
    }

    @GetMapping("/business")
    public List<PlannedTripResponse> getForBusiness(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getTripsByBusiness(user.getId());
    }

    @GetMapping("/available")
    public List<PlannedTripResponse> getAvailable(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getAvailableTrips(user.getId());
    }

    @PutMapping("/{reference}")
    public PlannedTripResponse closeTrip(@PathVariable("reference")String reference){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.closeTrip(reference, user.getId());
    }

    @PutMapping("/offer/{reference}/accept")
    public DeliveryRequestCreationResponse acceptTripOffer(@PathVariable("reference")String reference){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.acceptTripOffer(reference,user.getId());
    }

    @PutMapping("/offer/{reference}/reject")
    public TripOfferResponse rejectOffer(@PathVariable("reference")String reference){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.rejectOffer(reference, user.getEmail());
    }

    @GetMapping("/{tripRef}/offers")
    public List<TripOfferResponse> getAllOffers(@PathVariable("tripRef")String tripRef){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getAllTripOffers(tripRef, user.getId());
    }

    @GetMapping("/{tripRef}/offers/accepted")
    public List<TripOfferResponse> getAcceptedOffers(@PathVariable("tripRef")String tripRef){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getAcceptedTripOffers(tripRef,user.getId());
    }

    @GetMapping("/offer/{reference}")
    public TripOfferResponse getOffer(@PathVariable("reference")String reference){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getOffer(reference,user.getId());
    }

    @GetMapping("/{tripRef}/offer")
    public TripOfferResponse getTripOffer(@PathVariable("tripRef")String reference){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getTripOffer(reference, user.getId());
    }

    @PutMapping("/offer/negotiate")
    public TripOfferResponse negotiateOffer(@Validated @RequestBody NegotiateTripOfferRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.makeOffer(request.getReference(), MoneyUtilities.fromDoubleToMinor(request.getAmount()), user.getId());
    }

    @PostMapping("/offer")
    public TripOfferResponse createTripOffer(@Validated @RequestBody CreateTripOfferRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.createTripOffer(request, user.getId());
    }
}
