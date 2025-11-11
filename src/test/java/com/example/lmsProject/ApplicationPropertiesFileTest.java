package com.example.lmsProject;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesFileTest {

    private Properties loadProps() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            assertNotNull(in, "application.properties must be on the classpath (src/main/resources)");
            Properties p = new Properties();
            p.load(in);
            return p;
        }
    }

    @Test
    void fileExistsAndLoads() throws Exception {
        Properties p = loadProps();
        assertFalse(p.isEmpty(), "application.properties should not be empty");
    }

    @Test
    void requiredKeysPresent() throws Exception {
        Properties p = loadProps();
        assertTrue(hasText(p.getProperty("spring.application.name")), "spring.application.name required");
        assertTrue(hasText(p.getProperty("spring.datasource.url")), "spring.datasource.url required");
        assertTrue(hasText(p.getProperty("spring.datasource.username")), "spring.datasource.username required");
        assertTrue(hasText(p.getProperty("spring.datasource.password")), "spring.datasource.password required");
        assertTrue(hasText(p.getProperty("spring.jpa.hibernate.ddl-auto")), "spring.jpa.hibernate.ddl-auto required");
        assertTrue(hasText(p.getProperty("spring.jpa.show-sql")), "spring.jpa.show-sql required");
        assertTrue(hasText(p.getProperty("spring.jpa.properties.hibernate.dialect")), "hibernate dialect required");
        assertTrue(hasText(p.getProperty("logging.level.root")), "logging.level.root required");
        assertTrue(hasText(p.getProperty("logging.level.com.example.lmsProject")), "package log level required");
        assertTrue(hasText(p.getProperty("logging.level.org.springframework.web")), "spring web log level required");
    }

    @Test
    void valuesAreAsExpectedForThisProject() throws Exception {
        Properties p = loadProps();


        assertEquals("lmsProject", p.getProperty("spring.application.name"));
        assertEquals("jdbc:mysql://localhost:3306/lms", p.getProperty("spring.datasource.url"));
        assertEquals("root", p.getProperty("spring.datasource.username"));
        assertEquals("root", p.getProperty("spring.datasource.password"));
        assertEquals("validate", p.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("true", p.getProperty("spring.jpa.show-sql"));
        assertEquals("org.hibernate.dialect.MySQL8Dialect",
                p.getProperty("spring.jpa.properties.hibernate.dialect"));
        assertEquals("INFO", p.getProperty("logging.level.root"));
        assertEquals("DEBUG", p.getProperty("logging.level.com.example.lmsProject"));
        assertEquals("INFO", p.getProperty("logging.level.org.springframework.web"));

    }

    @Test
    void datasourceUrlLooksValid() throws Exception {
        Properties p = loadProps();
        String url = p.getProperty("spring.datasource.url");
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"), "URL should start with jdbc:mysql://");
        assertTrue(url.contains(":3306/"), "URL should contain default MySQL port 3306");
        assertTrue(url.endsWith("/lms") || url.contains("/lms?"),
                "URL should point to the 'lms' database");
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
