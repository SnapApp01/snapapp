package com.snappapp.snapng.snap.payment_util.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.snap.payment_util.paystack.AccountEnquiryResponse;
import com.snappapp.snapng.snap.payment_util.paystack.BankResponse;
import com.snappapp.snapng.snap.payment_util.paystack.GenericResponse;
import com.snappapp.snapng.snap.payment_util.paystack.InitialPaymentResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Builder
@Slf4j
public class PaystackClient {
    @Value("${paystack.base-url}")
    private String baseUrl;

    @Value("${paystack.secret-key}")
    private String secretKey;
    private RestTemplate restTemplate;

    public InitialPaymentResponse makeRequest(HttpMethod method, Object request, String path, Class clazz){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        HttpEntity<Object> entity = request == null ? new HttpEntity<>(headers) : new HttpEntity<>(request,headers);

        // Log the request being sent
        logRequest(method, path, request);

        try {
            log.info("Sending request to Paystack for {}", clazz.getSimpleName());
            ParameterizedTypeReference<GenericResponse<InitialPaymentResponse>> responseTypeRef = new ParameterizedTypeReference<GenericResponse<InitialPaymentResponse>>() {};
            ResponseEntity<GenericResponse<InitialPaymentResponse>> stringResp = restTemplate.exchange(baseUrl + path, method, entity, responseTypeRef);

            log.info("Received response: {}", new Gson().toJson(stringResp.getBody()));
            if (stringResp.getStatusCode().is2xxSuccessful()) {
                GenericResponse<InitialPaymentResponse> response = stringResp.getBody();
                if (response != null && response.isStatus()) {
                    return response.getData();
                }
                throw new FailedProcessException("Paystack API error: " + response.getMessage());
            }
            throw new FailedProcessException("HTTP Error: " + stringResp.getStatusCode().value());
        } catch (HttpStatusCodeException e) {
            log.error("Paystack request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FailedProcessException("Paystack request failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("An error occurred while making Paystack request: {}", e.getMessage(), e);
            throw new FailedProcessException("An unexpected error occurred while communicating with Paystack.");
        }
    }

    public AccountEnquiryResponse makeRequest(HttpMethod method, Object request, String path){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        HttpEntity<Object> entity = request == null ? new HttpEntity<>(headers) : new HttpEntity<>(request, headers);

        logRequest(method, path, request);

        try {
            log.info("Sending request to Paystack for account enquiry: {}", path);
            ParameterizedTypeReference<GenericResponse<AccountEnquiryResponse>> responseTypeRef = new ParameterizedTypeReference<GenericResponse<AccountEnquiryResponse>>() {};
            ResponseEntity<GenericResponse<AccountEnquiryResponse>> stringResp = restTemplate.exchange(baseUrl + path, method, entity, responseTypeRef);

            log.info("Received account enquiry response: {}", new Gson().toJson(stringResp.getBody()));
            if (stringResp.getStatusCode().is2xxSuccessful()) {
                GenericResponse<AccountEnquiryResponse> response = stringResp.getBody();
                if (response != null && response.isStatus()) {
                    return response.getData();
                }
                throw new FailedProcessException("Paystack API error: " + response.getMessage());
            }
            throw new FailedProcessException("HTTP Error: " + stringResp.getStatusCode().value());
        } catch (HttpStatusCodeException e) {
            log.error("Account enquiry request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FailedProcessException("Account enquiry failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("An error occurred while making Paystack account enquiry request: {}", e.getMessage(), e);
            throw new FailedProcessException("An unexpected error occurred while processing the account enquiry.");
        }
    }

    public List<BankResponse> makeRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching list of banks from Paystack.");
            ParameterizedTypeReference<GenericResponse<List<BankResponse>>> responseTypeRef = new ParameterizedTypeReference<GenericResponse<List<BankResponse>>>() {};
            ResponseEntity<GenericResponse<List<BankResponse>>> stringResp = restTemplate.exchange(baseUrl + "/bank?country=nigeria", HttpMethod.GET, entity, responseTypeRef);

            log.info("Received bank list response: {}", new Gson().toJson(stringResp.getBody()));
            if (stringResp.getStatusCode().is2xxSuccessful()) {
                GenericResponse<List<BankResponse>> response = stringResp.getBody();
                if (response != null && response.isStatus()) {
                    return response.getData();
                }
                throw new FailedProcessException("Paystack API error: " + response.getMessage());
            }
            throw new FailedProcessException("HTTP Error: " + stringResp.getStatusCode().value());
        } catch (HttpStatusCodeException e) {
            log.error("Bank list request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new FailedProcessException("Bank list request failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("An error occurred while fetching bank list from Paystack: {}", e.getMessage(), e);
            throw new FailedProcessException("An unexpected error occurred while fetching the bank list.");
        }
    }

    // Helper method to log request details
    private void logRequest(HttpMethod method, String path, Object request) {
        try {
            if (request != null) {
                log.info("Sending {} request to Paystack at path: {} with payload: {}", method, path, new ObjectMapper().writeValueAsString(request));
            } else {
                log.info("Sending {} request to Paystack at path: {} with no payload", method, path);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to log request body: {}", e.getMessage());
        }
    }
}