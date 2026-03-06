package com.example.lmsProject.phase1;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Phase 1 Test Suite
 *
 * Runs all Phase 1 tests together
 *
 * Tests included:
 * - RedisCachingIntegrationTest
 * - AsyncEmailTest
 * - Phase1ConfigurationTest
 * - PerformanceBenchmarkTest
 */
@Suite
@SelectClasses({
    RedisCachingIntegrationTest.class,
    AsyncEmailTest.class,
    Phase1ConfigurationTest.class,
    PerformanceBenchmarkTest.class
})
@DisplayName("Phase 1 Complete Test Suite")
class Phase1TestSuite {
    // This class runs all Phase 1 tests
}

