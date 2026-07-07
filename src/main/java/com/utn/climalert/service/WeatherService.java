package com.utn.climalert.service;

import com.utn.climalert.client.WeatherApiClient;
import com.utn.climalert.client.WeatherResponse;
import com.utn.climalert.model.WeatherRecord;
import com.utn.climalert.repository.WeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherRecordRepository repository;

    public WeatherRecord fetchAndSave() {
        WeatherResponse response = weatherApiClient.getCurrentWeather();

        WeatherRecord record = new WeatherRecord();
        record.setLocation(response.getLocation().getName());
        record.setTemperature(response.getCurrent().getTemp_c());
        record.setHumidity(response.getCurrent().getHumidity());
        record.setCondition(response.getCurrent().getCondition().getText());
        record.setTimestamp(LocalDateTime.now());

        return repository.save(record);
    }

    public Optional<WeatherRecord> getLatest() {
        return repository.findTopByOrderByTimestampDesc();
    }
}
