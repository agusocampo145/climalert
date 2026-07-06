package com.utn.climalert.repository;

import com.utn.climalert.model.WeatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {

    Optional<WeatherRecord> findTopByOrderByTimestampDesc();
}
