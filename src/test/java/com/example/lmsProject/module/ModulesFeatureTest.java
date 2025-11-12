package com.example.lmsProject.module;

import com.example.lmsProject.entity.Module;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.Controller.ModuleController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModulesFeatureTest {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void module_json_roundTrip_and_contract() throws Exception {
        Module m = new Module();
        Class<?> cls = m.getClass();

        setIfPresent(cls, m, List.of("ModuleId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                11, 11L, "11");

        setIfPresent(cls, m, List.of("Title", "Name", "ModuleTitle"),
                String.class, "Introduction to Java");

        setIfPresent(cls, m, List.of("Description", "Details", "Summary"),
                String.class, "Covers basics of syntax and OOP.");

        try {
            Course c = new Course();
            setIfPresent(Course.class, c, List.of("CourseId", "Id"),
                    anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                    101, 101L, "101");
            setIfPresent(cls, m, List.of("Course", "Clazz", "ParentCourse"), Course.class, c);
        } catch (Throwable ignored) {}

        setIfPresent(cls, m, List.of("CreatedAt", "CreatedOn", "CreatedDate"),
                LocalDateTime.class, LocalDateTime.of(2025, 1, 1, 9, 0, 0).withNano(0));

        String json = om.writeValueAsString(m);
        assertNotNull(json);
        Module back = (Module) om.readValue(json, cls);

        assertIfPresentEquals(cls, back, List.of("ModuleId", "Id"), 11, 11L, "11");
        assertIfPresentEquals(cls, back, List.of("Title", "Name", "ModuleTitle"), "Introduction to Java");
        assertIfPresentEquals(cls, back, List.of("Description", "Details", "Summary"), "Covers basics of syntax and OOP.");
    }

    @Test
    void controller_hasGetAllModules_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "module")
            );
        });
        assertTrue(found);
    }

    @Test
    void controller_hasGetModuleById_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found);
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

    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    private static boolean anyPathContains(Collection<String> paths, String needle) {
        return paths.stream().anyMatch(p -> p != null && p.toLowerCase().contains(needle.toLowerCase()));
    }

    private static boolean anyPathMatchesIdLike(Collection<String> paths) {
        return paths.stream().anyMatch(p ->
                p != null && (p.matches(".*\\{(?i:id)\\}.*") || p.matches(".*\\{[^/]+\\}.*"))
        );
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
            } catch (Exception ignored) {}
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
                assertEquals(expectedOptions[0], actual);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                fail("Getter invocation failed for '" + name + "': " + e);
            }
        }
    }
}
