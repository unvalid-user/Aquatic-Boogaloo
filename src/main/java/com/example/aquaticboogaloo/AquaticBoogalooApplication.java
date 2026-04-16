package com.example.aquaticboogaloo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AquaticBoogalooApplication {

	public static void main(String[] args) {
		SpringApplication.run(AquaticBoogalooApplication.class, args);

		System.out.println("Login: http://localhost:8080/login");
		System.out.println("Swagger: http://localhost:8080/swagger-ui/index.html");
	}

}
