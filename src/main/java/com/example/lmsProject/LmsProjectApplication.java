package com.example.lmsProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LmsProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(LmsProjectApplication.class, args);
	}

}
