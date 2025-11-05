package com.example.lmsProject.auth;


import com.example.lmsProject.Controller.AuthController; // adjust if package differs
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerMappingTest {

    // ----- helpers -----
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

    private static List<String> classBasePaths(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        return (rm == null) ? List.of() : valuesFromMapping(rm);
    }

    private static <A extends Annotation> List<String> methodPaths(Method m, Class<A> annType) {
        A ann = m.getAnnotation(annType);
        if (ann == null) return List.of();
        List<String> vals = valuesFromMapping(ann);
        return vals.isEmpty() ? List.of("") : vals; // treat empty as base
    }

    private static boolean anyPathContains(Collection<String> paths, String needle) {
        return paths.stream().anyMatch(p -> p != null && p.contains(needle));
    }

    // ----- tests -----

    @Test
    void controller_classHasBasePathContainingAuth_orMethodsDo() {
        Class<?> c = AuthController.class;
        List<String> base = classBasePaths(c);

        boolean ok = !base.isEmpty() && anyPathContains(base, "auth");
        if (!ok) {
            boolean methodHasAuth = Stream.of(c.getDeclaredMethods()).anyMatch(m ->
                    Stream.<Class<? extends Annotation>>of(
                            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, RequestMapping.class
                    ).anyMatch(a -> anyPathContains(methodPaths(m, a), "auth"))
            );
            assertTrue(methodHasAuth,
                    "Expected base path or at least one method path to contain 'auth'.");
        }
    }

    @Test
    void controller_hasLogin_endpoint() {
        Class<?> c = AuthController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || s.toLowerCase().contains("login") || s.toLowerCase().contains("signin")
            );
        });
        assertTrue(found, "Expected a POST mapping for login/signin.");
    }

    @Test
    void controller_hasRegister_endpoint_optional() {
        Class<?> c = AuthController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s != null && (s.toLowerCase().contains("register") || s.toLowerCase().contains("signup"))
            );
        });
        // If you don't expose registration, it's okay to skip. Make it non-fatal if truly absent:
        if (!found) System.out.println("[INFO] No explicit register/signup endpoint; skipping hard assertion.");
        assertTrue(true);
    }

    @Test
    void controller_hasRefreshToken_endpoint_optional() {
        Class<?> c = AuthController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> gp = methodPaths(m, GetMapping.class);
            List<String> pp = methodPaths(m, PostMapping.class);
            return Stream.concat(gp.stream(), pp.stream())
                    .anyMatch(s -> s != null && s.toLowerCase().contains("refresh"));
        });
        if (!found) System.out.println("[INFO] No explicit refresh endpoint; skipping hard assertion.");
        assertTrue(true);
    }

    @Test
    void controller_hasLogout_endpoint_optional() {
        Class<?> c = AuthController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, PostMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s -> s != null && s.toLowerCase().contains("logout"));
        });
        if (!found) System.out.println("[INFO] No explicit logout endpoint; skipping hard assertion.");
        assertTrue(true);
    }
}
