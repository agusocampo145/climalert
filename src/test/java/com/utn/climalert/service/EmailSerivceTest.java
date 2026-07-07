package com.utn.climalert.service;

import com.utn.climalert.model.WeatherRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "climalert@test.com");
        ReflectionTestUtils.setField(emailService, "recipients",
                new String[]{"admin@clima.com", "emergencias@clima.com", "meteorologia@clima.com"});
    }

    private WeatherRecord sampleRecord() {
        WeatherRecord r = new WeatherRecord();
        r.setLocation("Buenos Aires");
        r.setTemperature(38.5);
        r.setHumidity(70);
        r.setCondition("Sunny");
        return r;
    }

    @Test
    @DisplayName("Envía el correo a los 3 destinatarios de la consigna")
    void envia_aTresDestinatarios() {
        emailService.sendAlert(sampleRecord());

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly(
                "admin@clima.com", "emergencias@clima.com", "meteorologia@clima.com");
        assertThat(sent.getFrom()).isEqualTo("climalert@test.com");
    }

    @Test
    @DisplayName("El cuerpo del correo incluye el detalle del clima")
    void cuerpo_incluyeDetalleDelClima() {
        emailService.sendAlert(sampleRecord());

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body)
                .contains("Buenos Aires")   // ubicación
                .contains("38")             // temperatura
                .contains("70");            // humedad
    }

    @Test
    @DisplayName("Si el envío falla, NO propaga la excepción (la captura)")
    void noPropaga_siFallaElEnvio() {
        doThrow(new RuntimeException("SMTP caído"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendAlert(sampleRecord());

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
