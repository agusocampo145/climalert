package com.utn.climalert;

import com.utn.climalert.client.WeatherApiClient;
import com.utn.climalert.model.WeatherRecord;
import com.utn.climalert.service.WeatherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ClimalertApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClimalertApplication.class, args);
	}

	@Bean
	CommandLineRunner testPersistence(WeatherService weatherService) {
		return args -> {
			WeatherRecord saved = weatherService.fetchAndSave();
			System.out.println("✅ Guardado: " + saved);
		};
	}

}

