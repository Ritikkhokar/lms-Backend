package com.example.lmsProject.role;

import com.example.lmsProject.entity.Role;           // <-- adjust if your package differs
import com.example.lmsProject.Controller.RoleController; // <-- adjust if your package differs

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Single test file covering:
 *  1) Role model/DTO JSON contract (getters/setters + Jackson round-trip).
 *  2) RoleController endpoint mappings (annotation inspection).
 *
 * No Spring context is started; everything uses reflection & Jackson only.
 */
class RolesFeatureTest {

    // ---------------------------------------------------------------------
    // Jackson for DTO/Entity JSON tests
    // ---------------------------------------------------------------------
    private final ObjectMapper om = new ObjectMapper();

    // ---- Helpers for Role reflection (field-name variants) ----
    private Optional<Method> findGetter(Class<?> cls, List<String> candidates) {
        for (String name : candidates) {
            try {
                Method m = cls.getMethod("get" + name);
                m.setAccessible(true);
                return Optional.of(m);
            } catch (NoSuchMethodException ignored) {}
        }
        return Optional.empty();
    }

    private Optional<Method> findSetter(Class<?> cls, List<String> candidates, Class<?>... preferredTypes) {
        for (String name : candidates) {
            String setter = "set" + name;
            // Try preferred types first
            for (Class<?> t : preferredTypes) {
                try {
                    Method m = cls.getMethod(setter, t);
                    m.setAccessible(true);
                    return Optional.of(m);
                } catch (NoSuchMethodException ignored) {}
            }
            // Then any single-arg setter with that name
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    m.setAccessible(true);
                    return Optional.of(m);
                }
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------------
    // Role model/DTO tests (plain JUnit + Jackson)
    // ---------------------------------------------------------------------

    @Test
    void role_gettersAndSetters_work_withCommonVariants() {
        Role r = new Role();
        Class<?> cls = r.getClass();

        // Candidate property names
        List<String> ID   = Arrays.asList("Id", "RoleId");
        List<String> NAME = Arrays.asList("Name", "RoleName");
        List<String> DESC = Arrays.asList("Description", "Desc");

        // ID
        Optional<Method> setId = findSetter(cls, ID, Long.class, long.class, Integer.class, int.class, String.class);
        Optional<Method> getId = findGetter(cls, ID);
        Object expectedId = null;
        if (setId.isPresent() && getId.isPresent()) {
            Class<?> p = setId.get().getParameterTypes()[0];
            try {
                if (p == Long.class || p == long.class) expectedId = 1L;
                else if (p == Integer.class || p == int.class) expectedId = 1;
                else if (p == String.class) expectedId = "1";
                setId.get().invoke(r, expectedId);
                Object actualId = getId.get().invoke(r);
                assertEquals(expectedId, actualId, "ID getter/setter should round-trip");
            } catch (Exception e) {
                fail("Failed invoking ID getter/setter: " + e);
            }
        } else {
            System.out.println("[INFO] No ID/RoleId property found; skipping ID assertion.");
        }

        // NAME
        Optional<Method> setName = findSetter(cls, NAME, String.class);
        Optional<Method> getName = findGetter(cls, NAME);
        if (setName.isPresent() && getName.isPresent()) {
            try {
                setName.get().invoke(r, "TEACHER");
                Object actualName = getName.get().invoke(r);
                assertEquals("TEACHER", actualName, "Name/RoleName getter/setter should round-trip");
            } catch (Exception e) {
                fail("Failed invoking Name getter/setter: " + e);
            }
        } else {
            System.out.println("[INFO] No Name/RoleName property found; skipping name assertion.");
        }

        // DESC
        Optional<Method> setDesc = findSetter(cls, DESC, String.class);
        Optional<Method> getDesc = findGetter(cls, DESC);
        if (setDesc.isPresent() && getDesc.isPresent()) {
            try {
                setDesc.get().invoke(r, "Teacher role");
                Object actualDesc = getDesc.get().invoke(r);
                assertEquals("Teacher role", actualDesc, "Description/Desc getter/setter should round-trip");
            } catch (Exception e) {
                fail("Failed invoking Description getter/setter: " + e);
            }
        } else {
            System.out.println("[INFO] No Description/Desc property found; skipping description assertion.");
        }
    }

    @Test
    void role_json_roundTrip_preservesAvailableFields() throws Exception {
        Role r = new Role();
        Class<?> cls = r.getClass();

        List<String> ID   = Arrays.asList("Id", "RoleId");
        List<String> NAME = Arrays.asList("Name", "RoleName");
        List<String> DESC = Arrays.asList("Description", "Desc");

        // set present fields
        Object expectedId = null;
        Optional<Method> setId = findSetter(cls, ID, Long.class, long.class, Integer.class, int.class, String.class);
        if (setId.isPresent()) {
            Class<?> p = setId.get().getParameterTypes()[0];
            if (p == Long.class || p == long.class) expectedId = 7L;
            else if (p == Integer.class || p == int.class) expectedId = 7;
            else if (p == String.class) expectedId = "7";
            setId.get().invoke(r, expectedId);
        }

        String expectedName = null;
        Optional<Method> setName = findSetter(cls, NAME, String.class);
        if (setName.isPresent()) {
            expectedName = "ADMIN";
            setName.get().invoke(r, expectedName);
        }

        String expectedDesc = null;
        Optional<Method> setDesc = findSetter(cls, DESC, String.class);
        if (setDesc.isPresent()) {
            expectedDesc = "Administrative role";
            setDesc.get().invoke(r, expectedDesc);
        }

        // JSON round-trip
        String json = om.writeValueAsString(r);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));

        Role back = om.readValue(json, Role.class);
        assertNotNull(back);

        // verify present fields
        Optional<Method> getId = findGetter(cls, ID);
        if (getId.isPresent() && expectedId != null) {
            Object actualId = getId.get().invoke(back);
            assertEquals(expectedId, actualId, "ID should round-trip via JSON");
        }

        Optional<Method> getName = findGetter(cls, NAME);
        if (getName.isPresent() && expectedName != null) {
            Object actualName = getName.get().invoke(back);
            assertEquals(expectedName, actualName, "Name/RoleName should round-trip via JSON");
        }

        Optional<Method> getDesc = findGetter(cls, DESC);
        if (getDesc.isPresent() && expectedDesc != null) {
            Object actualDesc = getDesc.get().invoke(back);
            assertEquals(expectedDesc, actualDesc, "Description/Desc should round-trip via JSON");
        }
    }

    // ---------------------------------------------------------------------
    // RoleController mapping tests (annotation inspection only)
    // ---------------------------------------------------------------------

    // Mapping helpers
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
        // looks for "{id}" (case-insensitive) or any path variable
        return paths.stream().anyMatch(p ->
                p != null && (p.matches(".*\\{(?i:id)\\}.*") || p.matches(".*\\{[^/]+\\}.*"))
        );
    }

    private static List<String> classBasePaths(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        return (rm == null) ? List.of() : valuesFromMapping(rm);
    }

    // FIXED: if a mapping annotation is present with no explicit value/path,
    // treat it as mapping to the base path ("")
    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    @Test
    void controller_classHasBasePathContainingRoles_orMethodsDo() {
        Class<?> c = RoleController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "roles");
        if (!ok) {
            boolean methodHasRoles = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "roles"))
            );
            assertTrue(methodHasRoles,
                    "Expected base path or at least one method path to contain 'roles'. " +
                            "Add @RequestMapping(\"/api/roles\") on class or ensure method paths include 'roles'.");
        }
    }

    @Test
    void controller_hasGetAllRoles_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false; // not a GET method
            // ok if it's "", "/" (with class base) OR contains "roles"
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("roles")
            );
        });
        assertTrue(found, "Expected a GET mapping for listing roles (e.g., @GetMapping or @GetMapping(\"/api/roles\")).");
    }

    @Test
    void controller_hasGetRoleById_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping (e.g., @GetMapping(\"/{id}\") or \"/api/roles/{id}\").");
    }

    @Test
    void controller_hasCreateRole_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false; // not POST
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("roles")
            );
        });
        assertTrue(found, "Expected a POST mapping for creating a role.");
    }

    @Test
    void controller_hasUpdateRole_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PutMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a PUT mapping with an '{id}' path variable for updating a role.");
    }

    @Test
    void controller_hasDeleteRole_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, DeleteMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a DELETE mapping with an '{id}' path variable for deleting a role.");
    }
}
