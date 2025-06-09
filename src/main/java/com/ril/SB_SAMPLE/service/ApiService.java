package com.ril.SB_SAMPLE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ril.SB_SAMPLE.dto.response.ApiResponse;



@Service
public class ApiService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    Logger LOGGER = LogManager.getLogger(ApiService.class);

    public Mono<ApiResponse<String>> callPostApi(String url, Object requestPayload) {
        String currentMethodName = new Throwable().getStackTrace()[0].getMethodName();
        LOGGER.info("[ {} ] :: STARTED ", currentMethodName);
        LOGGER.info("[ {} ] :: URL :: [ {} ]", currentMethodName, url);
        LOGGER.info("[ {} ] :: Request Payload :: [ {} ]", currentMethodName, requestPayload);

        WebClient webClient = webClientBuilder.build();

        // Call the api.
        try {
            Mono<ApiResponse<String>> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestPayload)
                    .exchangeToMono(clientResponse -> {
                        Mono<String> bodyMono = clientResponse.bodyToMono(String.class);
                        return bodyMono.map(body -> new ApiResponse<>(clientResponse.statusCode().value(), body));
                    });
                LOGGER.info("[ {} ] :: ENDED ", currentMethodName);
                return response;    
        } catch (Exception e) {
            LOGGER.error("[ {} ] :: Exception :: [ {} ]", currentMethodName, e.getMessage());
            return null;
        }
    }
}