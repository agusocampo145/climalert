package com.utn.climalert.service;

import com.utn.climalert.model.WeatherRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertService {

    @Value("${alert.temperature-threshold}")
    private double temperatureThreshold;

    @Value("${alert.humidity-threshold}")
    private int humidityThreshold;

    private boolean alertActive = false;

    /**
     * Evalúa el registro y decide si corresponde ENVIAR una alerta.
     * Solo devuelve true en la transición  normal -> crítico.
     */
    public boolean shouldTriggerAlert(WeatherRecord record) {
        boolean critical = isCritical(record);

        if (critical && !alertActive) {
            alertActive = true;
            log.warn("🚨 Entrando en estado de ALERTA: {}°C, {}%",
                    record.getTemperature(), record.getHumidity());
            return true;
        }

        if (!critical && alertActive) {
            alertActive = false;
            log.info("✅ Condiciones normalizadas: {}°C, {}%",
                    record.getTemperature(), record.getHumidity());
        }

        return false;
    }

    /**
     * Regla de negocio
     * crítico si temperatura > umbral Y humedad > umbral.
     */
    public boolean isCritical(WeatherRecord record) {
        boolean tempCritical = record.getTemperature() > temperatureThreshold;
        boolean humidityCritical = record.getHumidity() > humidityThreshold;
        return tempCritical && humidityCritical;
    }
}
