package com.example.lmsProject.resource;

import com.example.lmsProject.Controller.ResourceController;
import com.example.lmsProject.entity.Resource;
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

class ResourcesFeatureTest {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void resource_json_roundTrip_and_contract() throws Exception {
        Resource r = new Resource();

        setIfPresent(r.getClass(), r, List.of("ResourceId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                101, 101L, "101");

        setIfPresent(r.getClass(), r, List.of("Title", "Name", "ResourceName"),
                anyOf(String.class), "Lecture Slides");

        setIfPresent(r.getClass(), r, List.of("Description", "Desc"),
                anyOf(String.class), "Week 1 intro slides");

        setIfPresent(r.getClass(), r, List.of("Url", "Link", "Path", "FileUrl", "ResourceUrl", "FilePath", "DownloadUrl", "Location"),
                anyOf(String.class), "https://example.com/r/lec1.pdf");

        setIfPresent(r.getClass(), r, List.of("Type", "ContentType", "ResourceType"),
                anyOf(String.class), "PDF");

        LocalDateTime now = LocalDateTime.of(2025, 1, 2, 12, 0, 0).withNano(0);
        setIfPresent(r.getClass(), r, List.of("CreatedAt", "CreatedOn", "CreatedDate"),
                anyOf(LocalDateTime.class), now);

        String json = om.writeValueAsString(r);
        assertNotNull(json);

        Resource back = om.readValue(json, Resource.class);
        assertNotNull(back);

        assertIfPresentEquals(back.getClass(), back, List.of("ResourceId", "Id"), 101, 101L, "101");
        assertIfPresentEquals(back.getClass(), back, List.of("Title", "Name", "ResourceName"), "Lecture Slides");
        assertIfPresentEquals(back.getClass(), back, List.of("Description", "Desc"), "Week 1 intro slides");
        assertIfPresentEquals(back.getClass(), back, List.of("Url", "Link", "Path", "FileUrl", "ResourceUrl", "FilePath", "DownloadUrl", "Location"), "https://example.com/r/lec1.pdf");
        assertIfPresentEquals(back.getClass(), back, List.of("Type", "ContentType", "ResourceType"), "PDF");
        assertIfPresentEquals(back.getClass(), back, List.of("CreatedAt", "CreatedOn", "CreatedDate"), now);
    }

    @Test
    void controller_hasGetAllResources_endpoint() {
        Class<?> c = ResourceController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "resource")
            );
        });
        assertTrue(found);
    }

    @Test
    void controller_hasGetResourceById_endpoint() {
        Class<?> c = ResourceController.class;
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
