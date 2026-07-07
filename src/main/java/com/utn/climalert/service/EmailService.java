package com.utn.climalert.service;

import com.utn.climalert.model.WeatherRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${alert.recipients}")
    private String[] recipients;

    public void sendAlert(WeatherRecord record) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipients);
            message.setSubject("ALERTA Climalert - Condiciones críticas en " + record.getLocation());
            message.setText(buildBody(record));

            mailSender.send(message);
            log.info("Correo de alerta enviado a {} destinatarios", recipients.length);
        } catch (Exception e) {
            log.error("Error al enviar el correo: {}", e.getMessage());
        }
    }

    private String buildBody(WeatherRecord record) {
        return """
                Se detectaron condiciones climáticas críticas.
                
                Ubicación:   %s
                Temperatura: %.1f °C
                Humedad:     %d %%
                Condición:   %s
                Registrado:  %s
                
                -- Servicio automático Climalert
                """.formatted(record.getLocation(), record.getTemperature(), record.getHumidity(), record.getCondition(), record.getTimestamp());
    }
}
