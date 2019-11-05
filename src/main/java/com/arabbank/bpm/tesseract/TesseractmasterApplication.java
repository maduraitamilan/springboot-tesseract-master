package com.arabbank.bpm.tesseract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TesseractmasterApplication {

	public static void main(String[] args) {		
		SpringApplication.run(TesseractmasterApplication.class, args);
	}

}
