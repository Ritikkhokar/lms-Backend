package com.example.lmsProject.submission;

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


import com.example.lmsProject.entity.Submission;
import com.example.lmsProject.Controller.SubmissionController;
import com.example.lmsProject.entity.User;


class SubmissionsFeatureTest {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @Test
    void submission_gettersSetters_jsonRoundTrip_flexible() throws Exception {
        Submission s = new Submission();

        // Try to set id with common variants: submissionId / id
        setIfPresent(s.getClass(), s, List.of("SubmissionId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                101, 101L, "101");


        User student = new User();
        setIfPresent(student.getClass(), student, List.of("UserId", "Id"),
                anyOf(Integer.class, int.class, Long.class, long.class, String.class),
                7, 7L, "7");
        setIfPresent(s.getClass(), s, List.of("Student", "User", "Author", "SubmittedBy"),
                User.class, student);


        setIfPresent(s.getClass(), s, List.of("SubmittedAt", "CreatedAt", "CreatedOn"),
                LocalDateTime.class, LocalDateTime.now().withNano(0));
        setIfPresent(s.getClass(), s, List.of("Grade", "Score"),
                anyOf(Integer.class, int.class, Double.class, double.class, String.class),
                95, 95.0, "A");
        setIfPresent(s.getClass(), s, List.of("Status", "State"), String.class, "SUBMITTED");
        setIfPresent(s.getClass(), s, List.of("FileUrl", "Url", "Link", "Path"),
                String.class, "https://cdn.example.com/submissions/101.pdf");
        setIfPresent(s.getClass(), s, List.of("Comments", "Feedback", "Notes"),
                String.class, "Well done!");

        String json = om.writeValueAsString(s);
        assertNotNull(json, "JSON should not be null");

        Submission back = om.readValue(json, Submission.class);
        assertNotNull(back, "Deserialized Submission should not be null");
    }




    @Test
    void controller_hasGetAllSubmissions_endpoint() {
        Class<?> c = SubmissionController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            if (p.isEmpty()) return false;
            return p.stream().anyMatch(s ->
                    s == null || s.isEmpty() || "/".equals(s) || anyPathContains(List.of(s), "submission"));
        });
        assertTrue(found, "Expected a GET mapping for listing submissions.");
    }

    @Test
    void controller_hasGetSubmissionById_endpoint() {
        Class<?> c = SubmissionController.class;
        boolean found = Stream.of(c.getDeclaredMethods()).anyMatch(m -> {
            List<String> p = methodPaths(m, GetMapping.class);
            return !p.isEmpty() && anyPathMatchesIdLike(p);
        });
        assertTrue(found, "Expected a GET-by-id mapping with '{id}'.");
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
                p != null && (p.matches(".*\\{(?i:id)\\}.*") || p.matches(".*\\{[^/]+\\}.*")));
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

    private static boolean hasAnyKey(Map<String, ?> map, String... keys) {
        for (String k : keys) if (map.containsKey(k)) return true;
        return false;
    }

    private static boolean hasAnyGetter(Class<?> cls, List<String> candidates) {
        for (String name : candidates) {
            try {
                Method g = cls.getMethod(name);
                if (g.getParameterCount() == 0) return true;
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
}
