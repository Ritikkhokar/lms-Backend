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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootTest(
        classes = LmsProjectApplicationTests.EmptyTestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.banner-mode=off",
                "spring.main.lazy-initialization=true"
        }
)
class LmsProjectApplicationTests {


    @SpringBootApplication
    static class EmptyTestApp {
    }

    @Autowired
    ApplicationContext context;

    @Test
    void minimalContextLoads() {
        assertNotNull(context, "Spring should bootstrap a minimal context");
    }


    @Test
    void hasSpringBootApplicationAnnotation() {
        assertTrue(
                LmsProjectApplication.class.isAnnotationPresent(SpringBootApplication.class),
                "@SpringBootApplication should be present on LmsProjectApplication"
        );
    }


    @Test
    void mainMethod_contract() throws Exception {

        Method m = LmsProjectApplication.class.getDeclaredMethod("main", String[].class);
        assertNotNull(m, "main(String[] args) should exist");

        assertEquals(void.class, m.getReturnType(), "main should return void");

        assertTrue(Modifier.isPublic(m.getModifiers()), "main should be public");

        assertTrue(Modifier.isStatic(m.getModifiers()), "main should be static");
    }

    @Test
    void mainMethod_runsWithoutExceptions_andCloses() {
        SpringApplication app = new SpringApplication(LmsProjectApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(WebApplicationType.NONE);
        assertDoesNotThrow(() -> {
            var ctx = app.run();
            ctx.close();
        });
    }
}


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


    @SpringBootApplication
    @RestController
    static class EmptyWebApp {
        @GetMapping("/ping")
        String ping() {
            return "pong";
        }
    }
}
