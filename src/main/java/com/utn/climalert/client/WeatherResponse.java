package com.utn.climalert.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private Location location;
    private Current current;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String name;
        private String localtime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        private double temp_c;
        private int humidity;
        private Condition condition;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Condition {
        private String text;
    }
}
