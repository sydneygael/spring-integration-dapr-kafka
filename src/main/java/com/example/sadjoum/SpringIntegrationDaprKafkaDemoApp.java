package com.example.sadjoum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
@EnableJpaRepositories(basePackages = "com.example.sadjoum.repository")
public class SpringIntegrationDaprKafkaDemoApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationDaprKafkaDemoApp.class, args);
	}

}
