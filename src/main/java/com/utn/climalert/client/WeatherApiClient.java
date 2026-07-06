package com.utn.climalert.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherApiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String location;

    public WeatherApiClient(
            @Value("${weatherapi.base-url}") String baseUrl,
            @Value("${weatherapi.key}") String apiKey,
            @Value("${weatherapi.location}") String location) {
        this.restClient = RestClient.create(baseUrl);
        this.apiKey = apiKey;
        this.location = location;
    }

    public WeatherResponse getCurrentWeather() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", location)
                        .queryParam("aqi", "no")
                        .build())
                .retrieve()
                .body(WeatherResponse.class);
    }
}
