package com.example.lmsProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized Retry Configuration for the LMS Application
 *
 * Defines retry policies, backoff strategies, and monitoring for:
 * - Email sending (SMTP failures)
 * - RabbitMQ operations (connection issues)
 * - Kafka operations (broker unavailable)
 * - External API calls (timeouts)
 */
@Configuration
@EnableRetry
public class RetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(RetryConfig.class);

    // ===== EMAIL RETRY POLICY =====
    // For: Email sending via SMTP (Gmail, etc.)
    // Retries: 3 times with exponential backoff (2s, 4s, 8s)
    // Used by: EmailServiceImpl.sendEmailWithRetry()

    @Bean(name = "emailRetryTemplate")
    public RetryTemplate emailRetryTemplate() {
        return createRetryTemplate(
            3,        // maxAttempts
            2000,     // initialInterval (2 seconds)
            10000,    // maxInterval (10 seconds max)
            2.0       // multiplier (exponential)
        );
    }

    @Bean(name = "rabbitmqRetryTemplate")
    public RetryTemplate rabbitmqRetryTemplate() {
        return createRetryTemplate(
            5,        // maxAttempts
            1000,     // initialInterval (1 second)
            15000,    // maxInterval (15 seconds max)
            1.5       // multiplier
        );
    }

    @Bean(name = "kafkaRetryTemplate")
    public RetryTemplate kafkaRetryTemplate() {
        return createRetryTemplate(
            4,        // maxAttempts
            1500,     // initialInterval (1.5 seconds)
            12000,    // maxInterval (12 seconds max)
            2.0       // multiplier
        );
    }

    @Bean(name = "externalApiRetryTemplate")
    public RetryTemplate externalApiRetryTemplate() {
        return createRetryTemplate(
            3,        // maxAttempts
            1000,     // initialInterval (1 second)
            8000,     // maxInterval (8 seconds max)
            2.0       // multiplier
        );
    }

    // ===== HELPER METHOD =====
    // Creates RetryTemplate with centralized configuration

    private RetryTemplate createRetryTemplate(
            int maxAttempts,
            long initialInterval,
            long maxInterval,
            double multiplier) {

        RetryTemplate retryTemplate = new RetryTemplate();

        // Set retry policy (how many times to retry)
        retryTemplate.setRetryPolicy(new MaxAttemptsRetryPolicy(maxAttempts));

        // Set backoff policy (wait time between retries)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Add retry listener for monitoring/logging
        retryTemplate.registerListener(new RetryMonitoringListener());

        return retryTemplate;
    }

    // ===== RETRY LISTENER FOR MONITORING =====
    // Logs and monitors all retry events
    // Implements modern RetryListener interface (not deprecated RetryListenerSupport)

    public static class RetryMonitoringListener implements RetryListener {

        private static final Logger logger = LoggerFactory.getLogger(RetryMonitoringListener.class);

        @Override
        public <T, E extends Throwable> void onSuccess(
                RetryContext context,
                RetryCallback<T, E> callback,
                T result) {

            int retryCount = context.getRetryCount();
            Object opName = context.getAttribute("operation.name");
            String operationName = (opName != null) ? opName.toString() : "Unknown";

            if (retryCount > 0) {
                logger.info("✅ Operation '{}' succeeded after {} retry attempt(s)",
                        operationName, retryCount);
            }
        }

        @Override
        public <T, E extends Throwable> void onError(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable) {

            int retryCount = context.getRetryCount();
            Object opName = context.getAttribute("operation.name");
            String operationName = (opName != null) ? opName.toString() : "Unknown";

            logger.warn("⚠️ Retry attempt {} failed for operation: {}. Error: {}",
                    retryCount, operationName, throwable.getMessage());
        }
    }

    // ===== FUTURE EXTENSION POINTS =====
    // When you add new services that need retry:
    // 1. Create new @Bean method similar to emailRetryTemplate()
    // 2. Configure appropriate maxAttempts, backoff, multiplier
    // 3. Inject into your service via @Autowired
    // 4. Use in programmatic retry logic if needed
    //
    // Example for Phase 2:
    // @Bean(name = "emailPublisherRetryTemplate")
    // public RetryTemplate emailPublisherRetryTemplate() { ... }
}

