package com.project.cook_mate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CookMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookMateApplication.class, args);
	}

}
