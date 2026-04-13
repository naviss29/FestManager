package com.festmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FestmanagerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestmanagerBackendApplication.class, args);
	}

}
