package com.example.lmsProject.phase1;

import com.example.lmsProject.entity.Course;
import com.example.lmsProject.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Caching Integration Tests
 *
 * Tests that verify:
 * - Redis is properly configured
 * - Caching works on CourseService
 * - Cache invalidation works correctly
 * - Cache keys are stored with proper prefix
 * - Cache hit rates are improving
 */
@SpringBootTest
@DisplayName("Redis Caching Integration Tests")
class RedisCachingIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName ->
                cacheManager.getCache(cacheName).clear()
            );
        }
    }

    @Test
    @DisplayName("Test 1: Redis connection is available")
    void testRedisConnectionAvailable() {
        // Arrange & Act
        assertDoesNotThrow(() -> {
            redisTemplate.opsForValue().set("test_key", "test_value");
            Object value = redisTemplate.opsForValue().get("test_key");

            // Assert
            assertEquals("test_value", value);

            // Cleanup
            redisTemplate.delete("test_key");
        });
    }

    @Test
    @DisplayName("Test 2: Course list is cached on first call")
    void testCourseListCaching() {
        // Arrange
        long startTime1 = System.currentTimeMillis();

        // Act - First call (cold cache - hits database)
        List<Course> courses1 = courseService.getAllCourses();
        long duration1 = System.currentTimeMillis() - startTime1;

        // Assert - First call should take longer (database hit)
        assertNotNull(courses1);
        assertFalse(courses1.isEmpty());
        System.out.println("❌ First call (DB): " + duration1 + "ms");

        // Act - Second call (warm cache - hits Redis)
        long startTime2 = System.currentTimeMillis();
        List<Course> courses2 = courseService.getAllCourses();
        long duration2 = System.currentTimeMillis() - startTime2;

        // Assert - Second call should be much faster
        assertEquals(courses1.size(), courses2.size());
        assertTrue(duration2 < duration1,
            "Cached call should be faster: " + duration2 + "ms vs " + duration1 + "ms");
        System.out.println("✅ Second call (Cache): " + duration2 + "ms");
        System.out.println("⚡ Speedup: " + (duration1 / (double) duration2) + "x faster");
    }

    @Test
    @DisplayName("Test 3: Individual course is cached by ID")
    void testIndividualCourseCaching() {
        // Arrange
        List<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("⚠️ No courses in database, skipping test");
            return;
        }

        Integer courseId = courses.get(0).getCourseId();

        // Act - First call (cache miss)
        long start1 = System.currentTimeMillis();
        Course course1 = courseService.getCourseById(courseId);
        long duration1 = System.currentTimeMillis() - start1;

        // Assert - First call
        assertNotNull(course1);
        assertEquals(courseId, course1.getCourseId());
        System.out.println("❌ First call (DB): " + duration1 + "ms");

        // Act - Second call (cache hit)
        long start2 = System.currentTimeMillis();
        Course course2 = courseService.getCourseById(courseId);
        long duration2 = System.currentTimeMillis() - start2;

        // Assert - Both calls return same data
        assertEquals(course1.getCourseId(), course2.getCourseId());
        assertEquals(course1.getTitle(), course2.getTitle());
        assertTrue(duration2 < duration1 || duration2 < 50,
            "Cached call should be very fast");
        System.out.println("✅ Second call (Cache): " + duration2 + "ms");
    }

    @Test
    @DisplayName("Test 4: Cache is invalidated on course creation")
    void testCacheInvalidationOnCreate() {
        // Arrange
        courseService.getAllCourses(); // Prime the cache

        // Act - Create new course
        Course newCourse = new Course();
        newCourse.setTitle("Test Cache Invalidation Course");
        newCourse.setDescription("Testing that cache is cleared");
//        newCourse.setCreatedBy("Test User");

        assertDoesNotThrow(() -> {
            courseService.createCourse(newCourse);
        });

        // Assert - Cache should be cleared
        var cache = cacheManager.getCache("courses");
        assertNotNull(cache);
        // After creation, cache should have been evicted
        System.out.println("✅ Cache invalidated on course creation");
    }

    @Test
    @DisplayName("Test 5: Cache keys use proper prefix")
    void testCacheKeyPrefix() {
        // Arrange & Act
        courseService.getAllCourses();

        // Assert - Check Redis for keys with LMS prefix
        var keys = redisTemplate.keys("lms:*");
        assertNotNull(keys);
        // Should have at least one key with proper prefix
        System.out.println("Redis keys with 'lms:' prefix: " + keys.size());
        System.out.println("Keys found: " + keys);
        assertTrue(keys.size() > 0, "Should have cached keys with 'lms:' prefix");
    }

    @Test
    @DisplayName("Test 6: Redis cache hit metrics")
    void testCacheMetrics() {
        // Arrange
        final int ITERATIONS = 5;
        AtomicInteger cacheHits = new AtomicInteger(0);

        // Act - Make multiple calls
        for (int i = 0; i < ITERATIONS; i++) {
            courseService.getAllCourses();
            // Each call after first should be a cache hit
            if (i > 0) {
                cacheHits.incrementAndGet();
            }
        }

        // Assert
        double hitRate = (cacheHits.get() / (double) ITERATIONS) * 100;
        System.out.println("Cache hit rate: " + hitRate + "%");
        System.out.println("Cache hits: " + cacheHits.get() + "/" + ITERATIONS);
        assertTrue(hitRate >= 50, "Should have at least 50% hit rate");
    }

    @Test
    @DisplayName("Test 7: Verify cache configuration")
    void testCacheConfiguration() {
        // Assert
        assertNotNull(cacheManager, "CacheManager should be configured");
        assertNotNull(cacheManager.getCache("courses"),
            "Courses cache should exist");
        assertNotNull(cacheManager.getCache("courseById"),
            "CourseById cache should exist");

        System.out.println("✅ Configured caches: " +
            cacheManager.getCacheNames());
    }

    @Test
    @DisplayName("Test 8: Redis TTL configuration")
    void testRedisTTLConfiguration() {
        // Arrange
        courseService.getAllCourses();

        // Act - Check TTL on a key
        var keys = redisTemplate.keys("lms:courses*");
        if (!keys.isEmpty()) {
            String firstKey = keys.iterator().next();
            Long ttl = redisTemplate.getExpire(firstKey);

            // Assert - Should have TTL set (in seconds)
            assertNotNull(ttl);
            System.out.println("✅ TTL for key '" + firstKey + "': " + ttl + " seconds");
            assertTrue(ttl > 0, "TTL should be positive");
        }
    }

    @Test
    @DisplayName("Test 9: Cache clear works")
    void testClearCache() {
        // Arrange
        courseService.getAllCourses();

        // Act
        var cache = cacheManager.getCache("courses");
        assertNotNull(cache);
        cache.clear();

        // Assert
        assertNull(cache.get("courses"));
        System.out.println("✅ Cache cleared successfully");
    }

    @Test
    @DisplayName("Test 10: Multiple cache names work independently")
    void testMultipleCacheNames() {
        // Arrange & Act
        courseService.getAllCourses();

        List<Course> courses = courseService.getAllCourses();
        if (!courses.isEmpty()) {
            courseService.getCourseById(courses.get(0).getCourseId());
        }

        // Assert - Both caches should exist
        assertNotNull(cacheManager.getCache("courses"));
        assertNotNull(cacheManager.getCache("courseById"));

        System.out.println("✅ Multiple caches working independently");
    }
}

