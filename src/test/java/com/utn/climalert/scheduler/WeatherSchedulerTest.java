package com.utn.climalert.scheduler;

import com.utn.climalert.model.WeatherRecord;
import com.utn.climalert.service.AlertService;
import com.utn.climalert.service.EmailService;
import com.utn.climalert.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherSchedulerTest {

    @Mock private WeatherService weatherService;
    @Mock private AlertService alertService;
    @Mock private EmailService emailService;

    @InjectMocks private WeatherScheduler scheduler;

    private WeatherRecord anyRecord() {
        WeatherRecord r = new WeatherRecord();
        r.setLocation("CABA");
        r.setTemperature(38.0);
        r.setHumidity(70);
        return r;
    }

    @Test
    @DisplayName("Envía correo cuando shouldTriggerAlert devuelve true")
    void envia_cuandoDebeAlertar() {
        WeatherRecord record = anyRecord();
        when(weatherService.getLatest()).thenReturn(Optional.of(record));
        when(alertService.shouldTriggerAlert(record)).thenReturn(true);

        scheduler.analyzeWeather();

        verify(emailService, times(1)).sendAlert(record);
    }

    @Test
    @DisplayName("NO envía correo cuando shouldTriggerAlert devuelve false")
    void noEnvia_cuandoNoDebeAlertar() {
        WeatherRecord record = anyRecord();
        when(weatherService.getLatest()).thenReturn(Optional.of(record));
        when(alertService.shouldTriggerAlert(record)).thenReturn(false);

        scheduler.analyzeWeather();

        verify(emailService, never()).sendAlert(any());
    }

    @Test
    @DisplayName("NO envía correo cuando no hay datos disponibles")
    void noEnvia_cuandoNoHayDatos() {
        when(weatherService.getLatest()).thenReturn(Optional.empty());

        scheduler.analyzeWeather();

        verify(emailService, never()).sendAlert(any());
        verify(alertService, never()).shouldTriggerAlert(any());
    }
}
