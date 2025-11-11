package com.example.lmsProject.buildchecks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class BuildGradleDependenciesTest {

    private static String gradle;

    @BeforeAll
    static void loadBuildGradle() throws Exception {
        Path p = Path.of("build.gradle");
        assertTrue(Files.exists(p), "build.gradle should exist in project root");
        gradle = Files.readString(p);
    }


    private static void assertGroup(String groupName, Map<String, String> descriptionToRegex) {
        StringBuilder failures = new StringBuilder();
        Pattern dotAll = Pattern.compile("", Pattern.DOTALL); // just to get DOTALL constant

        descriptionToRegex.forEach((desc, regex) -> {
            boolean found = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL)
                    .matcher(gradle)
                    .find();
            if (!found) {
                failures.append("\n - MISSING: ").append(desc)
                        .append(" (regex: ").append(regex).append(")");
            }
        });

        if (failures.length() > 0) {
            fail(groupName + " failed:" + failures);
        }
    }


    @Test
    @DisplayName("Plugins present in build.gradle")
    void pluginsPresent() {
        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("SonarQube plugin",
                "id\\s+[\"']org\\.sonarqube[\"']\\s+version\\s+[\"']7\\.0\\.0\\.6105[\"']");
        checks.put("Java plugin",
                "id\\s+[\"']java[\"']");
        checks.put("Spring Boot plugin",
                "id\\s+[\"']org\\.springframework\\.boot[\"']\\s+version\\s+[\"']3\\.5\\.6[\"']");
        checks.put("Dependency management plugin",
                "id\\s+[\"']io\\.spring\\.dependency-management[\"']\\s+version\\s+[\"']1\\.1\\.7[\"']");
        assertGroup("Plugins group", checks);
    }

    @Test
    @DisplayName("Dependencies present in build.gradle")
    void dependenciesPresent() {
        Map<String, String> checks = new LinkedHashMap<>();
        // Starters
        checks.put("Web starter",
                "implementation\\s+[\"']org\\.springframework\\.boot:spring-boot-starter-web[\"']");
        checks.put("JPA starter",
                "implementation\\s+[\"']org\\.springframework\\.boot:spring-boot-starter-data-jpa[\"']");
        checks.put("Security starter",
                "implementation\\s+[\"']org\\.springframework\\.boot:spring-boot-starter-security[\"']");
        // MySQL (explicit version line)
        checks.put("MySQL connector 8.0.33",
                "implementation\\s+[\"']mysql:mysql-connector-java:8\\.0\\.33[\"']");
        // Lombok
        checks.put("Lombok compileOnly 1.18.40",
                "compileOnly\\s+[\"']org\\.projectlombok:lombok:1\\.18\\.40[\"']");
        checks.put("Lombok annotationProcessor 1.18.40",
                "annotationProcessor\\s+[\"']org\\.projectlombok:lombok:1\\.18\\.40[\"']");
        // JJWT
        checks.put("jjwt-api 0.12.3",
                "implementation\\s+[\"']io\\.jsonwebtoken:jjwt-api:0\\.12\\.3[\"']");
        checks.put("jjwt-impl 0.12.3",
                "runtimeOnly\\s+[\"']io\\.jsonwebtoken:jjwt-impl:0\\.12\\.3[\"']");
        checks.put("jjwt-jackson 0.12.3",
                "runtimeOnly\\s+[\"']io\\.jsonwebtoken:jjwt-jackson:0\\.12\\.3[\"']");
        // Testing
        checks.put("spring-boot-starter-test",
                "testImplementation\\s+[\"']org\\.springframework\\.boot:spring-boot-starter-test[\"']");
        checks.put("junit-jupiter-params 5.10.2",
                "testImplementation\\s+[\"']org\\.junit\\.jupiter:junit-jupiter-params:5\\.10\\.2[\"']");
        checks.put("junit-platform-launcher (testRuntimeOnly)",
                "testRuntimeOnly\\s+[\"']org\\.junit\\.platform:junit-platform-launcher[\"']");
        assertGroup("Dependencies group", checks);
    }

    @Test
    @DisplayName("Test task configuration present")
    void testTaskConfigured() {
        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("JUnit Platform enabled in test task",
                "tasks\\.named\\('test'\\)\\s*\\{[\\s\\S]*?useJUnitPlatform\\(\\)[\\s\\S]*?\\}");
        checks.put("Readable test logging configured",
                "testLogging\\s*\\{[\\s\\S]*?events\\s+\"PASSED\",\\s*\"FAILED\",\\s*\"SKIPPED\"[\\s\\S]*?\\}");
        assertGroup("Test task configuration group", checks);
    }
}
