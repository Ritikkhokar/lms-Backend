package com.example.lmsProject;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApplicationPropertiesFileTest
 * Covers tests #5–#16 from the official unit testing document:
 * - Ensures application.properties exists and loads
 * - Validates presence of key Spring and logging properties
 * - Optionally checks server.port if defined
 */
class ApplicationPropertiesFileTest {

    private Properties loadProps() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            assertNotNull(in, "application.properties must be on the classpath (src/main/resources)");
            Properties p = new Properties();
            p.load(in);
            return p;
        }
    }

    /** Test #5: File exists & readable */
    @Test
    void file_exists_and_readable() throws Exception {
        Properties p = loadProps();
        assertFalse(p.isEmpty(), "application.properties should not be empty");
    }

    /** Test #6–#15: Required configuration keys present */
    @Test
    void required_keys_present() throws Exception {
        Properties p = loadProps();
        assertTrue(hasText(p.getProperty("spring.application.name")), "spring.application.name required");
        assertTrue(hasText(p.getProperty("spring.datasource.url")), "spring.datasource.url required");
        assertTrue(hasText(p.getProperty("spring.datasource.username")), "spring.datasource.username required");
        assertTrue(hasText(p.getProperty("spring.datasource.password")), "spring.datasource.password required");
        assertTrue(hasText(p.getProperty("spring.jpa.hibernate.ddl-auto")), "spring.jpa.hibernate.ddl-auto required");
        assertTrue(hasText(p.getProperty("spring.jpa.show-sql")), "spring.jpa.show-sql required");
        assertTrue(hasText(p.getProperty("spring.jpa.properties.hibernate.dialect")),
                "spring.jpa.properties.hibernate.dialect required");
        assertTrue(hasText(p.getProperty("logging.level.root")), "logging.level.root required");
        assertTrue(hasText(p.getProperty("logging.level.com.example.lmsProject")),
                "logging.level.com.example.lmsProject required");
        assertTrue(hasText(p.getProperty("logging.level.org.springframework.web")),
                "logging.level.org.springframework.web required");
    }

    /** Test #16: Optional server.port present and non-empty if defined */
    @Test
    void server_port_optional_present() throws Exception {
        Properties p = loadProps();
        String port = p.getProperty("server.port");
        if (port != null) {
            assertFalse(port.isBlank(), "server.port defined but empty");
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
