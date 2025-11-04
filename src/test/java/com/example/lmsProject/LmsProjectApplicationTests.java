package com.example.lmsProject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// extra imports for the port-check class below
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PART 1: Minimal Spring context tests (no DB, no web) – fast & deterministic.
 */
@SpringBootTest(
		classes = LmsProjectApplicationTests.EmptyTestApp.class, // minimal app, not your real beans
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"spring.main.banner-mode=off",
				"spring.main.lazy-initialization=true"
		}
)
class LmsProjectApplicationTests {

	// Minimal empty Boot app for framework-boot smoke testing
	@SpringBootApplication
	static class EmptyTestApp { }

	@Autowired
	ApplicationContext context;

	@Test
	void minimalContextLoads() {
		assertNotNull(context, "Spring should bootstrap a minimal context");
	}

	// ---- Added per your request ----
	@Test
	void hasSpringBootApplicationAnnotation() {
		assertTrue(
				LmsProjectApplication.class.isAnnotationPresent(SpringBootApplication.class),
				"@SpringBootApplication should be present on LmsProjectApplication"
		);
	}

	// ---- Added per your request (combined contract check) ----
	@Test
	void mainMethod_contract() throws Exception {
		// existence
		Method m = LmsProjectApplication.class.getDeclaredMethod("main", String[].class);
		assertNotNull(m, "main(String[] args) should exist");
		// return type
		assertEquals(void.class, m.getReturnType(), "main should return void");
		// visibility
		assertTrue(Modifier.isPublic(m.getModifiers()), "main should be public");
		// static
		assertTrue(Modifier.isStatic(m.getModifiers()), "main should be static");
	}

	@Test
	void mainMethod_runsWithoutExceptions_andCloses() {
		SpringApplication app = new SpringApplication(LmsProjectApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.setWebApplicationType(WebApplicationType.NONE);
		assertDoesNotThrow(() -> {
			var ctx = app.run(); // start
			ctx.close();         // close cleanly
		});
	}
}

/**
 * PART 2: Port check – spins up a tiny isolated web app on a RANDOM_PORT (no real repos/controllers).
 * This is integration-style but still avoids your real app beans & DB.
 */
@SpringBootTest(
		classes = PortCheckTest.EmptyWebApp.class,                 // tiny web app just for this test
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.main.banner-mode=off",
				"spring.main.lazy-initialization=true"
		}
)
class PortCheckTest {

	@LocalServerPort
	int port;

	@Autowired
	TestRestTemplate http;

	@Test
	void serverStartsOnRandomPort() {
		assertTrue(port > 0, "Embedded server should start on a random port");
	}

	@Test
	void pingEndpointRespondsOnThatPort() {
		var resp = http.getForEntity("http://localhost:" + port + "/ping", String.class);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertEquals("pong", resp.getBody());
	}

	@SpringBootApplication
	@RestController
	static class EmptyWebApp {
		@GetMapping("/ping")
		String ping() { return "pong"; }
	}
}
