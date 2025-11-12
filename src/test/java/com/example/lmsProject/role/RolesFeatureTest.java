package com.example.lmsProject.role;

import com.example.lmsProject.entity.Role;
import com.example.lmsProject.Controller.RoleController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class RolesFeatureTest {

    private final ObjectMapper om = new ObjectMapper();

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
            for (Class<?> t : preferredTypes) {
                try {
                    Method m = cls.getMethod(setter, t);
                    m.setAccessible(true);
                    return Optional.of(m);
                } catch (NoSuchMethodException ignored) {}
            }
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    m.setAccessible(true);
                    return Optional.of(m);
                }
            }
        }
        return Optional.empty();
    }

    @Test
    void role_json_roundTrip_and_contract() throws Exception {
        Role r = new Role();
        Class<?> cls = r.getClass();
        List<String> ID = Arrays.asList("Id", "RoleId");
        List<String> NAME = Arrays.asList("Name", "RoleName");
        List<String> DESC = Arrays.asList("Description", "Desc");

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

        String json = om.writeValueAsString(r);
        assertNotNull(json);
        Role back = om.readValue(json, Role.class);
        assertNotNull(back);

        Optional<Method> getId = findGetter(cls, ID);
        if (getId.isPresent() && expectedId != null) {
            Object actualId = getId.get().invoke(back);
            assertEquals(expectedId, actualId);
        }

        Optional<Method> getName = findGetter(cls, NAME);
        if (getName.isPresent() && expectedName != null) {
            Object actualName = getName.get().invoke(back);
            assertEquals(expectedName, actualName);
        }

        Optional<Method> getDesc = findGetter(cls, DESC);
        if (getDesc.isPresent() && expectedDesc != null) {
            Object actualDesc = getDesc.get().invoke(back);
            assertEquals(expectedDesc, actualDesc);
        }
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

    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals;
    }

    @Test
    void controller_hasGetAllRoles_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.contains("roles")
            );
        });
        assertTrue(found);
    }

    @Test
    void controller_hasGetRoleById_endpoint() {
        Class<?> c = RoleController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found);
    }
}
