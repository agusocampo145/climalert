package com.utn.climalert.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_records")
@Data
@NoArgsConstructor
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private double temperature;   // temp_c
    private int humidity;         // %
    private String condition;     // texto de la condición
    private LocalDateTime timestamp;  // cuándo lo registramos
}
