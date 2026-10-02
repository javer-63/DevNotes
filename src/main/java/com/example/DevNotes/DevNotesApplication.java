package com.example.DevNotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevNotesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevNotesApplication.class, args);
	}

}
