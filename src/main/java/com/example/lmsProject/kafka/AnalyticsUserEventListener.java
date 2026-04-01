package com.example.lmsProject.kafka;

import com.example.lmsProject.Repository.UserAnalyticsRepository;
import com.example.lmsProject.dto.UserEvent;
import com.example.lmsProject.entity.UserAnalytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AnalyticsUserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsUserEventListener.class);

    private final UserAnalyticsRepository userAnalyticsRepository;

    public AnalyticsUserEventListener(UserAnalyticsRepository userAnalyticsRepository) {
        this.userAnalyticsRepository = userAnalyticsRepository;
    }

    @KafkaListener(
            topics = "user-events",
            groupId = "lms-user-analytics-consumer"
    )
    public void onUserEventForAnalytics(UserEvent event) {
        Integer userId = event.getUserId();
        if (userId == null) {
            logger.warn("Analytics listener received event without userId, type={}", event.getEventType());
            return;
        }

        UserAnalytics analytics = userAnalyticsRepository.findById(userId)
                .orElseGet(() -> new UserAnalytics(userId));

        analytics.setEmail(event.getEmail());
        analytics.setFullName(event.getFullName());
        analytics.setRoleName(event.getRoleName());
        analytics.setLastEventType(event.getEventType());
        analytics.setLastEventTime(
                event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now()
        );
        analytics.setActive(!"USER_DELETED".equalsIgnoreCase(event.getEventType()));

        userAnalyticsRepository.save(analytics);

        logger.info("Analytics listener updated analytics for userId={} with eventType={}",
                userId, event.getEventType());
    }
}
