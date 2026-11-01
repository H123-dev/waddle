package com.h123.waddle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaddleApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaddleApplication.class, args);
	}

}
