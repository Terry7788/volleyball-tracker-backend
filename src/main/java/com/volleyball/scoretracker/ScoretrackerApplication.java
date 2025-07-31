package com.volleyball.scoretracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScoretrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScoretrackerApplication.class, args);
	}

}
