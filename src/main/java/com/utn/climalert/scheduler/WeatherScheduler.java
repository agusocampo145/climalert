package com.utn.climalert.scheduler;

import com.utn.climalert.model.WeatherRecord;
import com.utn.climalert.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherScheduler {

    private final WeatherService weatherService;

    @Scheduled(fixedRate = 300_000)  // 5 min = 300.000 ms
    public void fetchWeather() {
        try {
            WeatherRecord record = weatherService.fetchAndSave();
            log.info("Clima registrado: {}°C, {}% humedad, {}",
                    record.getTemperature(),
                    record.getHumidity(),
                    record.getCondition());
        } catch (Exception e) {
            log.error("Error al consultar/guardar el clima: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60_000)  // 1 min = 60.000 ms
    public void analyzeWeather() {
        weatherService.getLatest().ifPresentOrElse(
                record -> {
                    log.info("Analizando último registro: {}°C, {}%",
                            record.getTemperature(), record.getHumidity());
                    //TODO: lógica de alertas
                },
                () -> log.warn("Todavía no hay datos para analizar")
        );
    }
}
