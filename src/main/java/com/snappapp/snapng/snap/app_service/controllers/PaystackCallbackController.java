package com.snappapp.snapng.snap.app_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.snappapp.snapng.snap.app_service.apimodels.PaystackCallbackResponseData;
import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

@RequestMapping("/paystack/webhook")
@RestController
@Slf4j
public class PaystackCallbackController {

    private final WalletManagementService walletManagementService;
    private final PriceManagementService priceManagementService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaystackCallbackController(WalletManagementService walletManagementService, PriceManagementService priceManagementService) {
        this.walletManagementService = walletManagementService;
        this.priceManagementService = priceManagementService;
    }

//    @PostMapping
//    public ResponseEntity<Void> callback(
//            @RequestBody String payload,
//            @RequestHeader("x-paystack-signature") String signature
//    ) throws Exception {
//
//        // 1️⃣ Verify Paystack signature
//        if (!walletManagementService.verifyPaystackSignature(payload, signature)) {
//            log.warn("❌ Invalid Paystack signature");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//
//        // 2️⃣ Parse payload AFTER verification
//        LinkedHashMap<String, Object> body =
//                objectMapper.readValue(payload, LinkedHashMap.class);
//
//        // 3️⃣ Process payment
//        walletManagementService.callback(
//                objectMapper.convertValue(
//                        body.get("data"),
//                        PaystackCallbackResponseData.class
//                )
//        );
//
//        log.info("✅ Paystack callback processed successfully");
//        return ResponseEntity.ok().build();
//    }

    @PostMapping
    public void callback(@RequestBody LinkedHashMap<String, Object> response){
        log.info("Received Paystack callback response: {}", new Gson().toJson(response));
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
