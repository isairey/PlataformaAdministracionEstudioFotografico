package com.photostudio.photostudio_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhotostudioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotostudioBackendApplication.class, args);
	}

}
