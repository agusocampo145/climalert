package com.utn.climalert.service;

import com.utn.climalert.model.WeatherRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService();
        ReflectionTestUtils.setField(alertService, "temperatureThreshold", 35.0);
        ReflectionTestUtils.setField(alertService, "humidityThreshold", 60);
    }

    private WeatherRecord record(double temp, int humidity) {
        WeatherRecord r = new WeatherRecord();
        r.setLocation("Test City");
        r.setTemperature(temp);
        r.setHumidity(humidity);
        r.setCondition("Test");
        return r;
    }

    // ---------- Regla de negocio: isCritical ----------

    @Test
    @DisplayName("Es crítico cuando temperatura Y humedad superan el umbral")
    void esCritico_cuandoAmbosSuperanUmbral() {
        assertThat(alertService.isCritical(record(38.0, 70))).isTrue();
    }

    @Test
    @DisplayName("NO es crítico si solo la temperatura supera el umbral")
    void noEsCritico_soloTemperatura() {
        assertThat(alertService.isCritical(record(38.0, 50))).isFalse();
    }

    @Test
    @DisplayName("NO es crítico si solo la humedad supera el umbral")
    void noEsCritico_soloHumedad() {
        assertThat(alertService.isCritical(record(30.0, 70))).isFalse();
    }

    @Test
    @DisplayName("NO es crítico cuando ninguno supera el umbral")
    void noEsCritico_ninguno() {
        assertThat(alertService.isCritical(record(20.0, 40))).isFalse();
    }

    @Test
    @DisplayName("NO es crítico en el valor exacto del umbral (> estricto)")
    void noEsCritico_valorExacto() {
        assertThat(alertService.isCritical(record(35.0, 60))).isFalse();
    }

    // ---------- Estrategia de cambio de estado ----------

    @Test
    @DisplayName("Dispara alerta en la transición normal -> crítico")
    void disparaAlerta_enTransicion() {
        assertThat(alertService.shouldTriggerAlert(record(38.0, 70))).isTrue();
    }

    @Test
    @DisplayName("NO vuelve a disparar mientras se mantiene crítico (anti-spam)")
    void noRepite_mientrasSigueCritico() {
        assertThat(alertService.shouldTriggerAlert(record(38.0, 70))).isTrue();
        assertThat(alertService.shouldTriggerAlert(record(39.0, 72))).isFalse();
        assertThat(alertService.shouldTriggerAlert(record(37.0, 65))).isFalse();
    }

    @Test
    @DisplayName("Vuelve a disparar tras normalizarse y ponerse crítico de nuevo")
    void disparaDeNuevo_trasNuevoEvento() {
        assertThat(alertService.shouldTriggerAlert(record(38.0, 70))).isTrue();
        assertThat(alertService.shouldTriggerAlert(record(20.0, 40))).isFalse();
        assertThat(alertService.shouldTriggerAlert(record(36.0, 65))).isTrue();
    }

    @Test
    @DisplayName("No dispara mientras las condiciones siguen normales")
    void noDispara_condicionesNormales() {
        assertThat(alertService.shouldTriggerAlert(record(20.0, 40))).isFalse();
        assertThat(alertService.shouldTriggerAlert(record(25.0, 50))).isFalse();
    }
}
