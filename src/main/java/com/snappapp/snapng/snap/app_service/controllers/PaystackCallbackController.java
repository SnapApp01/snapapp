package com.snappapp.snapng.snap.app_service.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.snappapp.snapng.snap.app_service.apimodels.PaystackCallbackResponseData;
import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import com.snappapp.snapng.snap.payment_util.services.PaystackService;
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
    private final PaystackService paystackService;

    public PaystackCallbackController(WalletManagementService walletManagementService, PriceManagementService priceManagementService, PaystackService paystackService) {
        this.walletManagementService = walletManagementService;
        this.priceManagementService = priceManagementService;
        this.paystackService = paystackService;
    }
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature
    ) {
        log.info("Paystack webhook received");

        if (!paystackService.verifySignature(payload, signature)) {
            log.warn("Invalid Paystack signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();

            if (!"charge.success".equals(event)) {
                return ResponseEntity.ok().build();
            }

            PaystackCallbackResponseData data =
                    objectMapper.treeToValue(
                            root.path("data"),
                            PaystackCallbackResponseData.class
                    );

            walletManagementService.handlePaystackSuccess(data);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Failed to process Paystack webhook", e);
            return ResponseEntity.badRequest().build();
        }
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
//
//    @PostMapping
//    public void callback(@RequestBody LinkedHashMap<String, Object> response){
//        log.info("Received Paystack callback response: {}", new Gson().toJson(response));
//        String event = response.get("event").toString();
//        log.info(event);
//        walletManagementService.callback(new ObjectMapper().convertValue(response.get("data"), PaystackCallbackResponseData.class));
//    }


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
