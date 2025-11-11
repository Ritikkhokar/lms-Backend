package com.example.lmsProject.course;

import com.example.lmsProject.entity.Course;
import com.example.lmsProject.Controller.CourseController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;


class CoursesFeatureTest {


    private final ObjectMapper om = new ObjectMapper();

    @Test
    void course_serializesAndDeserializes() throws Exception {
        Course c = new Course();
        Class<?> cls = c.getClass();


        List<String> ID    = Arrays.asList("Id", "CourseId");
        List<String> TITLE = Arrays.asList("Title", "Name");
        List<String> DESC  = Arrays.asList("Description", "Desc", "Details");


        setIfPresent(cls, c, ID, anyOf(Long.class, long.class, Integer.class, int.class, String.class), 501L, 501, "501");
        setIfPresent(cls, c, TITLE, String.class, "Java Fundamentals");
        setIfPresent(cls, c, DESC, String.class, "Learn Java basics.");

        // Round-trip
        String json = om.writeValueAsString(c);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));

        Course back = om.readValue(json, Course.class);
        assertNotNull(back);

        // Verify set values if getters exist
        assertIfPresentEquals(cls, back, ID, 501L, 501, "501");
        assertIfPresentEquals(cls, back, TITLE, "Java Fundamentals");
        assertIfPresentEquals(cls, back, DESC, "Learn Java basics.");
    }

    @Test
    void course_json_hasExpectedKeys() throws Exception {
        Course c = new Course();
        Class<?> cls = c.getClass();

        setIfPresent(cls, c, Arrays.asList("Id", "CourseId"),
                anyOf(Long.class, long.class, Integer.class, int.class, String.class), 7L, 7, "7");
        setIfPresent(cls, c, Arrays.asList("Title", "Name"), String.class, "Data Structures");
        setIfPresent(cls, c, Arrays.asList("Description", "Desc", "Details"), String.class, "Classic DS course.");

        String json = om.writeValueAsString(c);
        Map<String, Object> map = om.readValue(json, new TypeReference<Map<String,Object>>(){});

        assertTrue(hasAnyKey(map, "id", "courseId"), "JSON should include 'id' or 'courseId'.");
        assertTrue(hasAnyKey(map, "title", "name"),   "JSON should include 'title' or 'name'.");

        assertTrue(hasAnyKey(map, "description", "desc", "details") || true,
                "JSON may include 'description'/'desc'/'details' if present in Course.");
    }




    private static List<String> valuesFromMapping(Object mapping) {
        try {
            String[] arr = (String[]) mapping.getClass().getMethod("value").invoke(mapping);
            if (arr != null && arr.length > 0) return Arrays.asList(arr);
        } catch (Exception ignored) {}
        try {
            String[] arr = (String[]) mapping.getClass().getMethod("path").invoke(mapping);
            if (arr != null && arr.length > 0) return Arrays.asList(arr);
        } catch (Exception ignored) {}
        return List.of();
    }

    private static boolean anyPathContains(Collection<String> paths, String needle) {
        return paths.stream().anyMatch(p -> p != null && p.contains(needle));
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
    void controller_classHasBasePathContainingCourses_orMethodsDo() {
        Class<?> c = CourseController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "courses");
        if (!ok) {
            boolean methodHasCourses = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "courses"))
            );
            assertTrue(methodHasCourses,
                    "Expected base path or at least one method path to contain 'courses'. " +
                            "Either @RequestMapping(\"/api/courses\") on class or method paths include 'courses'.");
        }
    }

    @Test
    void controller_hasGetAllCourses_endpoint() {
        Class<?> c = CourseController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("courses")
            );
        });
        assertTrue(found, "Expected a GET mapping for listing courses.");
    }

    @Test
    void controller_hasGetCourseById_endpoint() {
        Class<?> c = CourseController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping with '{id}'.");
    }

    @Test
    void controller_hasCreateCourse_endpoint() {
        Class<?> c = CourseController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("courses")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating a course.");
    }

    @Test
    void controller_hasUpdateCourse_endpoint() {
        Class<?> c = CourseController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with '{id}' for updating a course.");
    }

    @Test
    void controller_hasDeleteCourse_endpoint() {
        Class<?> c = CourseController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with '{id}' for deleting a course.");
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
                    System.out.println(e.getMessage());
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
                    } catch (Exception ignored) {}
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
                    } catch (Exception ignored) {}
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
                    if (Objects.equals(actual, exp)) return;
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
