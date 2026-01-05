package com.snappapp.snapng.config.paystack;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PaystackHttpClient {

    private final PaystackConfig config;
    private final WebClient client = WebClient.builder().build();

    public <T> Mono<T> post(String path, Object body, Class<T> responseType) {
        return client.post()
            .uri(config.getBaseUrl() + path)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getSecretKey())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> get(String path, Class<T> responseType) {
        return client.get()
            .uri(config.getBaseUrl() + path)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getSecretKey())
            .retrieve()
            .bodyToMono(responseType);
    }
}
