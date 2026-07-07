package com.utn.climalert.scheduler;

import com.utn.climalert.model.WeatherRecord;
import com.utn.climalert.service.AlertService;
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

    private final AlertService alertService;

    @Scheduled(fixedRate = 60_000)  // 5 min = 300.000 ms
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
                    if (alertService.isCritical(record)) {
                        log.warn("¡ALERTA! Condiciones críticas: {}°C, {}%",
                                record.getTemperature(), record.getHumidity());
                        // TODO: el envío de correo
                    } else {
                        log.info("Condiciones normales: {}°C, {}%",
                                record.getTemperature(), record.getHumidity());
                    }
                },
                () -> log.warn("⚠️ Todavía no hay datos para analizar")
        );
    }
}
