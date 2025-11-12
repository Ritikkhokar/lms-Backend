package com.example.lmsProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
    static class EmptyTestApp {}

    /**
     * Test 1: Verify that @SpringBootApplication is present on main class.
     */
    @Test
    void hasSpringBootApplicationAnnotation() {
        assertTrue(
                LmsProjectApplication.class.isAnnotationPresent(SpringBootApplication.class),
                "@SpringBootApplication should be present on LmsProjectApplication"
        );
    }

    /**
     * Test 2: Verify that main(String[] args) exists, is public, static, and returns void.
     */
    @Test
    void mainMethod_contract() throws Exception {
        Method m = LmsProjectApplication.class.getDeclaredMethod("main", String[].class);
        assertNotNull(m, "main(String[] args) should exist");
        assertEquals(void.class, m.getReturnType(), "main should return void");
        assertTrue(Modifier.isPublic(m.getModifiers()), "main should be public");
        assertTrue(Modifier.isStatic(m.getModifiers()), "main should be static");
    }

    /**
     * Test 3: Ensure Spring starts successfully in NON-WEB mode without exceptions.
     */
    @Test
    void main_starts_in_non_web_mode() {
        SpringApplication app = new SpringApplication(LmsProjectApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(WebApplicationType.NONE);
        assertDoesNotThrow(() -> {
            var ctx = app.run();
            ctx.close();
        }, "SpringApplication should start and close cleanly in NON-WEB mode");
    }
}
