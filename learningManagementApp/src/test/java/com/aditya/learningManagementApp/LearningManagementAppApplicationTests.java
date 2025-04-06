package com.aditya.learningManagementApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests if the Spring application context loads successfully.
 */
@SpringBootTest // This annotation triggers the loading of the complete application context.
class LearningManagementAppApplicationTests {

	@Test
	void contextLoads() {
		// The test assertion is implicit: if the context loads, the test passes.
		// No specific assertions are needed inside the method body for this purpose.
	}

}