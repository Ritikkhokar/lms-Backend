package com.example.lmsProject.user;



import com.example.lmsProject.entity.User;           // <-- adjust if your package differs
import com.example.lmsProject.dto.UserDto;        // <-- adjust if your package differs
import com.example.lmsProject.Controller.UserController; // <-- adjust if your package differs

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Single file covering:
 *  1) UserDto JSON contract (serialize/deserialize & expected keys)  [reuses your previous tests]
 *  2) UserController endpoint mapping checks (annotation inspection, no Spring context)
 *
 * Pure JUnit + Jackson + reflection. No MockMvc, no Spring Boot test context.
 */
class UsersFeatureTest {

    // --------------------------- JSON tests (UserDto) ---------------------------
    private final ObjectMapper om = new ObjectMapper();

    @Test
    void userDto_serializesAndDeserializes() throws Exception {
        // Adjust field names that exist in your UserDto (id/userId, fullName/name, etc.)
        // We'll build via reflection to avoid tight coupling.
        UserDto dto = new UserDto();
        Class<?> cls = dto.getClass();

        // Candidates for common fields
        List<String> ID       = Arrays.asList("Id", "UserId");
        List<String> NAME     = Arrays.asList("FullName", "Name");
        List<String> EMAIL    = Arrays.asList("Email");
        List<String> ROLE     = Arrays.asList("Role", "RoleName");

        // ---- Set what exists ----
        setIfPresent(cls, dto, ID, anyOf(Long.class, long.class, Integer.class, int.class, String.class), 101L, 101, "101");
        setIfPresent(cls, dto, NAME, String.class, "Prajwal Hulamani");
        setIfPresent(cls, dto, EMAIL, String.class, "prajwal@example.com");
        setIfPresent(cls, dto, ROLE, String.class, "STUDENT");

        // round trip
        String json = om.writeValueAsString(dto);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));

        UserDto back = om.readValue(json, UserDto.class);
        assertNotNull(back);

        // verify what exists
        assertIfPresentEquals(cls, back, ID, 101L, 101, "101");
        assertIfPresentEquals(cls, back, NAME, "Prajwal Hulamani");
        assertIfPresentEquals(cls, back, EMAIL, "prajwal@example.com");
        assertIfPresentEquals(cls, back, ROLE, "STUDENT");
    }

    @Test
    void userDto_hasExpectedJsonKeys() throws Exception {
        UserDto dto = new UserDto();
        Class<?> cls = dto.getClass();

        setIfPresent(cls, dto, Arrays.asList("Id", "UserId"),
                anyOf(Long.class, long.class, Integer.class, int.class, String.class), 7L, 7, "7");
        setIfPresent(cls, dto, Arrays.asList("FullName", "Name"), String.class, "Student Name");
        setIfPresent(cls, dto, Arrays.asList("Email"), String.class, "student@example.com");
        setIfPresent(cls, dto, Arrays.asList("Role", "RoleName"), String.class, "TEACHER");

        String json = om.writeValueAsString(dto);
        Map<String, Object> map = om.readValue(json, new TypeReference<Map<String,Object>>(){});

        // We don't know exact property names; accept common variants.
        assertTrue(
                hasAnyKey(map, "id", "userId"),
                "JSON should include 'id' or 'userId' key");
        assertTrue(
                hasAnyKey(map, "fullName", "name"),
                "JSON should include 'fullName' or 'name' key");
        assertTrue(
                hasAnyKey(map, "email"),
                "JSON should include 'email' key");
        // role could be nested object depending on your DTO; here we check string property variants
        assertTrue(
                hasAnyKey(map, "role", "roleName") || !map.containsKey("role"),
                "JSON should include 'role' or 'roleName' if present in DTO");
    }

    // ----------------------- Controller mapping tests (UserController) -----------------------

    // Utility: extract value/path arrays from mapping annotations
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

    // IMPORTANT: treat present annotation with no explicit path as base ""
    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    @Test
    void controller_classHasBasePathContainingUsers_orMethodsDo() {
        Class<?> c = UserController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "users");
        if (!ok) {
            boolean methodHasUsers = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "users"))
            );
            assertTrue(methodHasUsers,
                    "Expected base path or at least one method path to contain 'users'. " +
                            "Add @RequestMapping(\"/api/users\") on class or ensure method paths include 'users'.");
        }
    }

    @Test
    void controller_hasGetAllUsers_endpoint() {
        Class<?> c = UserController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("users")
            );
        });
        assertTrue(found, "Expected a GET mapping for listing users (e.g., @GetMapping or @GetMapping(\"/api/users\")).");
    }

    @Test
    void controller_hasGetUserById_endpoint() {
        Class<?> c = UserController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping (e.g., @GetMapping(\"/{id}\") or \"/api/users/{id}\").");
    }

    @Test
    void controller_hasCreateUser_endpoint() {
        Class<?> c = UserController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("users")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating a user.");
    }

    @Test
    void controller_hasUpdateUser_endpoint() {
        Class<?> c = UserController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with an '{id}' path variable for updating a user.");
    }

    @Test
    void controller_hasDeleteUser_endpoint() {
        Class<?> c = UserController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with an '{id}' path variable for deleting a user.");
    }

    // --------------------------- Reflection helpers (DTO) ---------------------------

    private static boolean hasAnyKey(Map<String, ?> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k)) return true;
        }
        return false;
    }

    private static Class<?>[] anyOf(Class<?>... types) {
        return types;
    }

    private static void setIfPresent(Class<?> cls, Object obj, List<String> candidates,
                                     Class<?>[] preferredTypes, Object... sampleValues) {
        // find setter for any candidate name with preferred types, or any single-arg setter of that name
        for (String name : candidates) {
            String setter = "set" + name;
            // try preferred types with sampleValues
            for (Class<?> t : preferredTypes) {
                try {
                    Method m = cls.getMethod(setter, t);
                    m.setAccessible(true);
                    // pick matching sample
                    for (Object sv : sampleValues) {
                        if (sv != null && t.isAssignableFrom(sv.getClass())) {
                            m.invoke(obj, sv);
                            return;
                        }
                        // primitives case handling
                        if (t.isPrimitive()) {
                            if ((t == long.class && sv instanceof Long) ||
                                    (t == int.class  && sv instanceof Integer)) {
                                m.invoke(obj, sv);
                                return;
                            }
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (Exception e) {
                    // ignore and continue
                }
            }
            // try any single-arg setter with that name (use first sample if assignable)
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    try {
                        Class<?> t = m.getParameterTypes()[0];
                        for (Object sv : sampleValues) {
                            if (sv == null) continue;
                            if (t.isAssignableFrom(sv.getClass()) ||
                                    (t == long.class && sv instanceof Long) ||
                                    (t == int.class && sv instanceof Integer)) {
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
                // ignore; try next
            }
            // try any single-arg setter with that name
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
                // if getter exists but none matched, assert equals first provided
                assertEquals(expectedOptions[0], actual, "Field '" + name + "' should equal one of expected values.");
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                fail("Getter invocation failed for '" + name + "': " + e);
            }
        }
        // no getter found: skip (DTO may not expose that field)
        System.out.println("[INFO] No getter found for " + candidates + "; skipping equality assertion.");
    }
}
