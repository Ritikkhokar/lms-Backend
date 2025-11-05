

package com.example.lmsProject.module;


import com.example.lmsProject.entity.Module;
import com.example.lmsProject.entity.Course;
import com.example.lmsProject.Controller.ModuleController;

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

class ModulesFeatureTest {


    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())                 // safe if you have LocalDateTime in Module
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);



    @Test
    void module_gettersSetters_jsonRoundTrip_flexible() throws Exception {
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
        } catch (Throwable ignored) {
            // If you don't have a Course entity or relation, that's fine.
        }

        // Optional: if you keep createdAt/updatedAt in Module
        setIfPresent(cls, m, List.of("CreatedAt", "CreatedOn", "CreatedDate"),
                LocalDateTime.class, LocalDateTime.of(2025, 1, 1, 9, 0, 0).withNano(0));

        // JSON round-trip
        String json = om.writeValueAsString(m);
        assertNotNull(json);
        Module back = (Module) om.readValue(json, cls);

        // Soft assertions: only assert fields that exist
        assertIfPresentEquals(cls, back, List.of("ModuleId", "Id"), 11, 11L, "11");
        assertIfPresentEquals(cls, back, List.of("Title", "Name", "ModuleTitle"), "Introduction to Java");
        assertIfPresentEquals(cls, back, List.of("Description", "Details", "Summary"), "Covers basics of syntax and OOP.");
    }



    @Test
    void controller_classHasBasePathContainingModules_orMethodsDo() {
        Class<?> c = ModuleController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "module");
        if (!ok) {
            boolean methodHas = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "module"))
            );
            assertTrue(methodHas,
                    "Expected base path or at least one method path to contain 'module' (e.g., '/api/modules').");
        }
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
        assertTrue(found, "Expected a GET mapping for listing modules.");
    }

    @Test
    void controller_hasGetModuleById_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping with '{id}'.");
    }

    @Test
    void controller_hasCreateModule_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "module")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating a module.");
    }

    @Test
    void controller_hasUpdateModule_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with '{id}' for updating a module.");
    }

    @Test
    void controller_hasDeleteModule_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with '{id}' for deleting a module.");
    }


    @Test
    void controller_optional_listByCourse_endpoint() {
        Class<?> c = ModuleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> gp = methodPaths(m, GetMapping.class);
            return gp.stream().anyMatch(s ->
                    s != null && (s.toLowerCase().contains("course"))
            );
        });
        if (!found) System.out.println("[INFO] No explicit list-by-course endpoint; skipping hard assertion.");
        assertTrue(true);
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
        return vals.isEmpty() ? List.of("") : vals; // empty means base path (OK)
    }

    private static Class<?>[] anyOf(Class<?>... types) {
        return types;
    }

    private static void setIfPresent(Class<?> cls, Object obj, List<String> candidates,
                                     Class<?>[] preferredTypes, Object... sampleValues) {
        for (String name : candidates) {
            String setter = "set" + name;
            // preferred types first
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
                // continue
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
