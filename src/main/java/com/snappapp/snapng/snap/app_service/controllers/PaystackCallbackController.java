package com.snappapp.snapng.snap.app_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.snappapp.snapng.snap.app_service.apimodels.PaystackCallbackResponseData;
import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

@RequestMapping("/api/v1/payments/callback")
@RestController
@Slf4j
public class PaystackCallbackController {

    private final WalletManagementService walletManagementService;
    private final PriceManagementService priceManagementService;

    public PaystackCallbackController(WalletManagementService walletManagementService, PriceManagementService priceManagementService) {
        this.walletManagementService = walletManagementService;
        this.priceManagementService = priceManagementService;
    }

    @PostMapping
    public void callback(@RequestBody LinkedHashMap<String, Object> response){
        log.info(new Gson().toJson(response));
        String event = response.get("event").toString();
        log.info(event);
        walletManagementService.callback(new ObjectMapper().convertValue(response.get("data"), PaystackCallbackResponseData.class));
    }

    @PutMapping("/reverse/{reference}")
    public void reverse(@PathVariable("reference") String reference){
        walletManagementService.reverse(reference);
    }

    @GetMapping("/{trackId}")
    public void temp(@PathVariable("trackId")String trackId){
        log.info(trackId);
        priceManagementService.processPayment(trackId);
    }
}
