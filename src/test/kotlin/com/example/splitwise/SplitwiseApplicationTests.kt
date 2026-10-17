package com.example.splitwise

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
	properties = [
		"spring.datasource.url=jdbc:h2:mem:splitwise-test;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=test-secret-key-at-least-thirty-two-characters-long",
	],
)
class SplitwiseApplicationTests {

	@Test
	fun contextLoads() {
	}
}
