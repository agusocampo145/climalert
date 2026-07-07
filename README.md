# 🌦️ Climalert

Servicio de monitoreo climático que consulta periódicamente el estado del tiempo,
guarda un histórico y envía alertas por correo cuando las condiciones se vuelven
críticas.

Trabajo práctico desarrollado con **Spring Boot**.

---

## ¿Qué hace?

La idea es simple: tener un servicio que corra solo, sin que nadie lo esté mirando,
y que avise por mail cuando el clima se pone feo.

Concretamente:

- Consulta el clima de una ubicación fija (**CABA**) usando la API de WeatherAPI.
- Cada **5 minutos** obtiene los datos actuales y los guarda en la base.
- Cada **1 minuto** analiza la última medición disponible.
- Si detecta condiciones críticas (**temperatura > 35°C Y humedad > 60%**),
  manda un correo de alerta a los responsables.

---

## Cómo está armado

Separé la lógica en capas para que cada cosa tenga su responsabilidad:

- **`WeatherService`** → habla con la API externa y persiste los registros.
- **`AlertService`** → decide si una medición es crítica y si corresponde alertar.
- **`EmailService`** → arma y envía el correo.
- **`WeatherScheduler`** → el "director de orquesta": dispara las tareas periódicas
  y conecta las piezas.

---

## Decisiones de diseño (lo que me pareció importante resolver bien)

### 1. Alertas por cambio de estado (para no spamear)

Este fue el punto que más me hizo pensar. Si simplemente mandara un correo cada vez
que la medición es crítica, con el análisis corriendo cada minuto terminaría enviando
un mail por minuto mientras dure la ola de calor. Un desastre. 😅

Entonces el `AlertService` recuerda el estado anterior:

- Manda el correo **solo en la transición** de "normal" a "crítico".
- Mientras las condiciones sigan críticas, **no repite** la alerta.
- Recién cuando todo se normaliza y vuelve a ponerse crítico, avisa de nuevo.

Así el usuario recibe un aviso por cada *evento*, no uno por cada medición.

### 2. Envío de correo asíncrono

El envío del mail lo hago con `@Async` sobre un pool de hilos propio. La razón es
concreta: durante las pruebas, un problema de conexión con el servidor SMTP dejó
**colgado el scheduler entero**, porque el envío bloqueaba el único hilo de las
tareas programadas.

Sacando el envío a otro hilo, el análisis periódico sigue funcionando pase lo que
pase con el correo.

### 3. Timeouts en el correo

De la mano de lo anterior: le puse timeouts de conexión/lectura/escritura al cliente
de mail. Sin eso, si el servidor SMTP no responde, la app se queda esperando para
siempre. Con los timeouts, falla rápido, lo loguea y sigue viva.

### 4. Manejo de errores

Tanto la consulta a la API como el envío de correo están envueltos en manejo de
errores, para que un fallo puntual (API caída, red inestable) no tire abajo el
servicio.

---

## Configuración

El proyecto necesita algunas variables. Los datos sensibles (API key y credenciales
de correo) **no están hardcodeados**: se cargan desde variables de entorno.

### Variables de entorno (No incluidas en el proyecto porque use las personales)

| Variable | Descripción |
|----------|-------------|
| `WEATHER_API_KEY` | API key de WeatherAPI |
| `MAIL_USERNAME` | Cuenta de correo del remitente |
| `MAIL_PASSWORD` | Contraseña de aplicación (no la contraseña normal) |

### `application.properties` (valores principales)

```properties
# Umbrales de alerta
alert.temperature-threshold=35
alert.humidity-threshold=60

# Destinatarios
alert.recipients=admin@clima.com,emergencias@clima.com,meteorologia@clima.com

# Frecuencias (en milisegundos)
# Registro de clima: cada 5 min | Análisis: cada 1 min
```

> **Nota sobre Gmail:** hay que activar la verificación en 2 pasos y generar una
> "contraseña de aplicación" de 16 caracteres. La contraseña normal de la cuenta
> no funciona con SMTP.

---

## Cómo correrlo

1. Configurar las variables de entorno mencionadas arriba.
2. Levantar la aplicación:

   ```bash
   mvn spring-boot:run
   ```

3. El servicio arranca solo y empieza a registrar y analizar el clima según los
   intervalos configurados.

---

## Tests

Incluí tests unitarios de la lógica principal:

- **`AlertServiceTest`** → la regla de negocio y, sobre todo, la estrategia de
  cambio de estado (que no repita alertas).
- **`EmailServiceTest`** → verifica el armado del correo y el manejo de errores,
  usando Mockito para no enviar mails reales.
- **`WeatherSchedulerTest`** → que la orquestación conecte bien las piezas.

Para correrlos:

```bash
mvn test
```

---

## Stack

- Java + Spring Boot
- Spring Scheduling (`@Scheduled`)
- Spring Async (`@Async`)
- Spring Data JPA (persistencia del histórico)
- Java Mail Sender
- JUnit 5 + Mockito (tests)
