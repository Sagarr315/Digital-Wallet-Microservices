package com.digitalwallet.walletservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    public Long getUserIdFromToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "http://AUTH-SERVICE/auth/validate",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                return responseBody.get("userId").asLong();
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }
}