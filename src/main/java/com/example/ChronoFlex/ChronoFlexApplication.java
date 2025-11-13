package com.example.ChronoFlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChronoFlexApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronoFlexApplication.class, args);
	}

}
