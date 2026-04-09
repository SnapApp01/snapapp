package com.snappapp.snapng.snap.app_service.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snappapp.snapng.snap.app_service.apimodels.PaystackCallbackResponseData;
import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import com.snappapp.snapng.snap.payment_util.services.PaystackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/paystack/webhook")
@RestController
@Slf4j
public class PaystackCallbackController {

    private final WalletManagementService walletManagementService;
    private final PriceManagementService priceManagementService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaystackService paystackService;

    public PaystackCallbackController(WalletManagementService walletManagementService,
                                      PriceManagementService priceManagementService,
                                      PaystackService paystackService) {
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

        // Verify signature for security
        if (!paystackService.verifySignature(payload, signature)) {
            log.warn("Invalid Paystack signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText();

            log.info("Processing webhook event: {}", event);

            // Route to appropriate handler based on event type
            switch (event) {
                case "charge.success":
                    handleChargeSuccess(root);
                    break;

                case "transfer.success":
                    handleTransferSuccess(root);
                    break;

                case "transfer.failed":
                    handleTransferFailed(root);
                    break;

                case "transfer.reversed":
                    handleTransferReversed(root);
                    break;

                default:
                    log.info("Unhandled event type: {}", event);
                    // Return 200 for unhandled events to prevent Paystack from retrying
                    return ResponseEntity.ok().build();
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Failed to process Paystack webhook", e);
            // Return 200 even on error to prevent Paystack from retrying
            // (log the error and handle manually if needed)
            return ResponseEntity.ok().build();
        }
    }

    private void handleChargeSuccess(JsonNode root) throws Exception {
        log.info("Processing charge.success event");

        PaystackCallbackResponseData data = objectMapper.treeToValue(
                root.path("data"),
                PaystackCallbackResponseData.class
        );

        walletManagementService.handlePaystackSuccess(data);
    }

    private void handleTransferSuccess(JsonNode root) throws Exception {
        log.info("Processing transfer.success event");

        JsonNode dataNode = root.path("data");
        String reference = dataNode.path("reference").asText();
        String transferCode = dataNode.path("transfer_code").asText();
        Long amount = dataNode.path("amount").asLong();

        walletManagementService.handleTransferSuccess(reference, transferCode, amount);
    }

    private void handleTransferFailed(JsonNode root) throws Exception {
        log.info("Processing transfer.failed event");

        JsonNode dataNode = root.path("data");
        String reference = dataNode.path("reference").asText();
        String failureReason = dataNode.path("failures") != null && !dataNode.path("failures").isNull()
                ? dataNode.path("failures").toString()
                : dataNode.path("gateway_response").asText("Unknown failure");

        walletManagementService.handleTransferFailure(reference, failureReason);
    }

    private void handleTransferReversed(JsonNode root) throws Exception {
        log.info("Processing transfer.reversed event");

        JsonNode dataNode = root.path("data");
        String reference = dataNode.path("reference").asText();

        walletManagementService.handleTransferReversal(reference);
    }

    // Keep your existing endpoints
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

//package com.snappapp.snapng.snap.app_service.controllers;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.snappapp.snapng.snap.app_service.apimodels.PaystackCallbackResponseData;
//import com.snappapp.snapng.snap.app_service.services.PriceManagementService;
//import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
//import com.snappapp.snapng.snap.payment_util.services.PaystackService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.LinkedHashMap;
//
//@RequestMapping("/paystack/webhook")
//@RestController
//@Slf4j
//public class PaystackCallbackController {
//
//    private final WalletManagementService walletManagementService;
//    private final PriceManagementService priceManagementService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final PaystackService paystackService;
//
//    public PaystackCallbackController(WalletManagementService walletManagementService, PriceManagementService priceManagementService, PaystackService paystackService) {
//        this.walletManagementService = walletManagementService;
//        this.priceManagementService = priceManagementService;
//        this.paystackService = paystackService;
//    }
//
//    @PostMapping
//    public ResponseEntity<Void> handleWebhook(
//            @RequestBody String payload,
//            @RequestHeader("x-paystack-signature") String signature
//    ) {
//        log.info("Paystack webhook received");
//
//        if (!paystackService.verifySignature(payload, signature)) {
//            log.warn("Invalid Paystack signature");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        try {
//            JsonNode root = objectMapper.readTree(payload);
//            String event = root.path("event").asText();
//            log.info("Processing webhook event: {}", event);
//
//            if (!"charge.success".equals(event)) {
//                return ResponseEntity.ok().build();
//            }
//
//            PaystackCallbackResponseData data =
//                    objectMapper.treeToValue(
//                            root.path("data"),
//                            PaystackCallbackResponseData.class
//                    );
//
//            walletManagementService.handlePaystackSuccess(data);
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            log.error("Failed to process Paystack webhook", e);
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PutMapping("/reverse/{reference}")
//    public void reverse(@PathVariable("reference") String reference){
//        walletManagementService.reverse(reference);
//    }
//
//    @GetMapping("/{trackId}")
//    public void temp(@PathVariable("trackId")String trackId){
//        log.info(trackId);
//        priceManagementService.processPayment(trackId);
//    }
//}
