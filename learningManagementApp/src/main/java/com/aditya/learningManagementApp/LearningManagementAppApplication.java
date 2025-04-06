package com.aditya.learningManagementApp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningManagementAppApplication {

	private static final Logger logger = LogManager.getLogger(LearningManagementAppApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Learning Management Application...");
		SpringApplication.run(LearningManagementAppApplication.class);
		logger.info("Learning Management Application started successfully.");
	}
}
