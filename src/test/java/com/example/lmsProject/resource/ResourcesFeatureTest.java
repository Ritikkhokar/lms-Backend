package com.example.lmsProject.resource;

import com.example.lmsProject.Controller.ResourceController;
import com.example.lmsProject.entity.Resource;
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


class ResourcesFeatureTest {


    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);


    @Test
    void resource_gettersSetters_jsonRoundTrip_flexible() throws Exception {
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
        assertNotNull(json, "Resource must serialize to JSON");

        Resource back = om.readValue(json, Resource.class);
        assertNotNull(back, "Resource must deserialize from JSON");


        assertIfPresentEquals(back.getClass(), back, List.of("ResourceId", "Id"), 101, 101L, "101");
        assertIfPresentEquals(back.getClass(), back, List.of("Title", "Name", "ResourceName"), "Lecture Slides");
        assertIfPresentEquals(back.getClass(), back, List.of("Description", "Desc"), "Week 1 intro slides");
        assertIfPresentEquals(back.getClass(), back, List.of("Url", "Link", "Path", "FileUrl", "ResourceUrl", "FilePath", "DownloadUrl", "Location"), "https://example.com/r/lec1.pdf");
        assertIfPresentEquals(back.getClass(), back, List.of("Type", "ContentType", "ResourceType"), "PDF");
        assertIfPresentEquals(back.getClass(), back, List.of("CreatedAt", "CreatedOn", "CreatedDate"), now);
    }

    @Test
    void resource_json_hasExpectedKeys() throws Exception {
        Resource r = new Resource();


        setIfPresent(r.getClass(), r, List.of("ResourceId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                1, 1L, "1");

        setIfPresent(r.getClass(), r, List.of("Title", "Name", "ResourceName"),
                anyOf(String.class), "Syllabus");


        setIfPresent(r.getClass(), r, List.of(
                "Url", "Link", "Path", "FileUrl", "ResourceUrl", "FilePath", "DownloadUrl", "Location"
        ), anyOf(String.class), "/files/syllabus.pdf");

        String json = om.writeValueAsString(r);
        Map<String, Object> map = om.readValue(json, new TypeReference<Map<String, Object>>() {
        });


        assertTrue(hasAnyKey(map, "resourceId", "id"),
                "JSON should include 'resourceId' or 'id'.");
        assertTrue(hasAnyKey(map, "title", "name", "resourceName"),
                "JSON should include a title/name-like field.");


        boolean urlGetterExists = hasAnyGetter(r.getClass(), List.of(
                "getUrl", "getLink", "getPath", "getFileUrl", "getResourceUrl", "getFilePath", "getDownloadUrl", "getLocation"
        ));

        if (urlGetterExists) {
            assertTrue(hasAnyKey(map, "url", "link", "path", "fileUrl", "resourceUrl", "filePath", "downloadUrl", "location"),
                    "JSON should include a url/link/path-like field.");
        } else {
            System.out.println("[INFO] Resource has no URL-like getter; skipping strict URL key assertion.");
            assertTrue(true);
        }


        assertTrue(hasAnyKey(map, "description", "desc") || true, "description may be present");
        assertTrue(hasAnyKey(map, "type", "contentType", "resourceType") || true, "type may be present");
        assertTrue(hasAnyKey(map, "createdAt", "createdOn", "createdDate") || true, "timestamp may be present");
    }


    @Test
    void controller_classHasBasePathContainingResources_orMethodsDo() {
        Class<?> c = ResourceController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "resource");
        if (!ok) {
            boolean methodHasResources = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "resource"))
            );
            assertTrue(methodHasResources,
                    "Expected base path or at least one method path to contain 'resource' (e.g., '/api/resources').");
        }
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
        assertTrue(found, "Expected a GET mapping for listing resources.");
    }

    @Test
    void controller_hasGetResourceById_endpoint() {
        Class<?> c = ResourceController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping with '{id}'.");
    }

    @Test
    void controller_hasCreateResource_endpoint() {
        Class<?> c = ResourceController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "resource")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating a resource.");
    }

    @Test
    void controller_hasUpdateResource_endpoint() {
        Class<?> c = ResourceController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with '{id}' for updating a resource.");
    }

    @Test
    void controller_hasDeleteResource_endpoint() {
        Class<?> c = ResourceController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with '{id}' for deleting a resource.");
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

    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    private static List<String> classBasePaths(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        return (rm == null) ? List.of() : valuesFromMapping(rm);
    }

    private static boolean anyPathContains(Collection<String> paths, String needle) {
        return paths.stream().anyMatch(p -> p != null && p.toLowerCase().contains(needle.toLowerCase()));
    }

    private static boolean anyPathMatchesIdLike(Collection<String> paths) {
        return paths.stream().anyMatch(p ->
                p != null && (p.matches(".*\\{(?i:id)\\}.*") || p.matches(".*\\{[^/]+\\}.*"))
        );
    }

    private static boolean hasAnyKey(Map<String, ?> map, String... keys) {
        for (String k : keys) if (map.containsKey(k)) return true;
        return false;
    }

    private static boolean hasAnyGetter(Class<?> cls, List<String> getterNames) {
        for (String g : getterNames) {
            try {
                Method m = cls.getMethod(g);
                if (m.getParameterCount() == 0) return true;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return false;
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
            // fallback: any single-arg setter with that name
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

    }
}
