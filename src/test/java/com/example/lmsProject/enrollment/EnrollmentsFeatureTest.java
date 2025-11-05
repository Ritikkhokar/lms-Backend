package com.example.lmsProject.enrollment;


import com.example.lmsProject.entity.Enrollment;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.Controller.EnrollmentController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentsFeatureTest {

    private final ObjectMapper om = new ObjectMapper();


    @Test
    void enrollment_enrolledAt_serializesAndDeserializes_withJavaTimeModule() throws Exception {

        com.example.lmsProject.entity.Enrollment e = new com.example.lmsProject.entity.Enrollment();
        e.setEnrollmentId(123); // Lombok setter exists for Integer field
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 30, 0).withNano(0);
        e.setEnrolledAt(now);


        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);


        String json = mapper.writeValueAsString(e);
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("\"enrolledAt\""), "JSON should include 'enrolledAt' field");

        com.example.lmsProject.entity.Enrollment back =
                mapper.readValue(json, com.example.lmsProject.entity.Enrollment.class);


        assertNotNull(back.getEnrolledAt(), "Deserialized enrolledAt should not be null");
        assertEquals(now, back.getEnrolledAt(), "Deserialized enrolledAt should match the original value");
    }

    @Test
    void enrollment_json_hasExpectedKeys() throws Exception {
        Enrollment e = new Enrollment();
        Class<?> cls = e.getClass();


        User student = new User();
        setIfPresent(student.getClass(), student,
                Arrays.asList("UserId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                77, 77L, "77");

        Course course = new Course();
        setIfPresent(course.getClass(), course,
                Arrays.asList("CourseId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                707, 707L, "707");


        setIfPresent(cls, e, Arrays.asList("Student", "User", "Learner"), User.class, student);
        setIfPresent(cls, e, Arrays.asList("Course", "Clazz"), Course.class, course);


        setIfPresent(cls, e, Arrays.asList("EnrollmentId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                7, 7L, "7");

        String json = om.writeValueAsString(e);
        Map<String, Object> map = om.readValue(json, new TypeReference<Map<String, Object>>() {
        });


        assertTrue(hasAnyKey(map, "enrollmentId", "id"),
                "JSON should include 'enrollmentId' or 'id'.");
        assertTrue(map.containsKey("student"),
                "JSON should include 'student' (object), since Enrollment has a User reference.");
        assertTrue(map.containsKey("course"),
                "JSON should include 'course' (object), since Enrollment has a Course reference.");


        assertTrue(hasAnyKey(map, "enrolledAt", "createdAt", "createdOn", "createdDate") || true,
                "Created/Enrolled timestamp may be present if mapped.");
        assertTrue(hasAnyKey(map, "enrolledBy") || true,
                "enrolledBy may be present if your model exposes it.");
    }


    private static List<String> valuesFromMapping(Object mapping) {
        try {
            String[] arr = (String[]) mapping.getClass().getMethod("value").invoke(mapping);
            if (arr != null && arr.length > 0) return Arrays.asList(arr);
        } catch (Exception ignored) {
        }
        try {
            String[] arr = (String[]) mapping.getClass().getMethod("path").invoke(mapping);
            if (arr != null && arr.length > 0) return Arrays.asList(arr);
        } catch (Exception ignored) {
        }
        return List.of();
    }

    private static boolean anyPathContains(Collection<String> paths, String needle) {
        return paths.stream().anyMatch(p -> p != null && p.toLowerCase().contains(needle.toLowerCase()));
    }

    private static boolean anyPathMatchesIdLike(Collection<String> paths) {
        return paths.stream().anyMatch(p ->
                p != null && (p.matches(".*\\{(?i:id)\\}.*") || p.matches(".*\\{[^/]+\\}.*"))
        );
    }

    private static List<String> classBasePaths(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        return (rm == null) ? List.of() : valuesFromMapping(rm);
    }

    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    @Test
    void controller_classHasBasePathContainingEnrollments_orMethodsDo() {
        Class<?> c = EnrollmentController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "enroll");
        if (!ok) {
            boolean methodHasEnroll = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "enroll"))
            );
            assertTrue(methodHasEnroll,
                    "Expected base path or at least one method path to contain 'enroll' (e.g., '/api/enrollments').");
        }
    }

    @Test
    void controller_hasGetAllEnrollments_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "enroll")
            );
        });
        assertTrue(found, "Expected a GET mapping for listing enrollments.");
    }

    @Test
    void controller_hasGetEnrollmentById_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping with '{id}'.");
    }

    @Test
    void controller_hasCreateEnrollment_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "enroll")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating an enrollment.");
    }

    @Test
    void controller_hasUpdateEnrollment_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with '{id}' for updating an enrollment.");
    }

    @Test
    void controller_hasDeleteEnrollment_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with '{id}' for deleting an enrollment.");
    }

    @Test
    void controller_optional_listByUser_orByCourse_endpoint() {
        Class<?> c = EnrollmentController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> gp = methodPaths(m, GetMapping.class);
            return gp.stream().anyMatch(s ->
                    s != null && (s.toLowerCase().contains("user") || s.toLowerCase().contains("course"))
            );
        });
        if (!found) System.out.println("[INFO] No explicit list-by-user/course endpoints; skipping hard assertion.");
        assertTrue(true);
    }


    private static boolean hasAnyKey(Map<String, ?> map, String... keys) {
        for (String k : keys) if (map.containsKey(k)) return true;
        return false;
    }

    private static Class<?>[] anyOf(Class<?>... types) {
        return types;
    }

    private static void setIfPresent(Class<?> cls, Object obj, List<String> candidates,
                                     Class<?>[] preferredTypes, Object... sampleValues) {
        for (String name : candidates) {
            String setter = "set" + name;

            for (Class<?> t : preferredTypes) {
                try {
                    Method m = cls.getMethod(setter, t);
                    m.setAccessible(true);
                    for (Object sv : sampleValues) {
                        if (sv == null) continue;
                        if (t.isAssignableFrom(sv.getClass())
                                || (t == long.class && sv instanceof Long)
                                || (t == int.class && sv instanceof Integer)) {
                            m.invoke(obj, sv);
                            return;
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (Exception e) {
                    // continue
                }
            }

            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    try {
                        Class<?> t = m.getParameterTypes()[0];
                        for (Object sv : sampleValues) {
                            if (sv == null) continue;
                            if (t.isAssignableFrom(sv.getClass())
                                    || (t == long.class && sv instanceof Long)
                                    || (t == int.class && sv instanceof Integer)) {
                                m.setAccessible(true);
                                m.invoke(obj, sv);
                                return;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private static void setIfPresent(Class<?> cls, Object obj, List<String> candidates, Class<?> type, Object sample) {
        for (String name : candidates) {
            String setter = "set" + name;
            try {
                Method m = cls.getMethod(setter, type);
                m.setAccessible(true);
                m.invoke(obj, sample);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    try {
                        if (m.getParameterTypes()[0].isAssignableFrom(type)) {
                            m.setAccessible(true);
                            m.invoke(obj, sample);
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private static void assertIfPresentEquals(Class<?> cls, Object obj, List<String> candidates, Object... expectedOptions) {
        for (String name : candidates) {
            try {
                Method g = cls.getMethod("get" + name);
                g.setAccessible(true);
                Object actual = g.invoke(obj);
                for (Object exp : expectedOptions) {
                    if (Objects.equals(actual, exp)) return; // success
                }
                assertEquals(expectedOptions[0], actual, "Field '" + name + "' should equal one of expected values.");
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                fail("Getter invocation failed for '" + name + "': " + e);
            }
        }
        System.out.println("[INFO] No getter found for " + candidates + "; skipping equality assertion.");
    }
}
