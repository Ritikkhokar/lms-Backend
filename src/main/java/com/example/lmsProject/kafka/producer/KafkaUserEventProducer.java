package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.config.KafkaTopicsConfig;
import com.example.lmsProject.dto.UserEvent;
import com.example.lmsProject.service.UserEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaUserEventProducer implements UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaUserEventProducer.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public KafkaUserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishUserEvent(UserEvent event) {
        String key = event.getUserId() != null ? event.getUserId().toString() : "unknown";
        logger.info("Publishing user event {} for userId={}", event.getEventType(), key);
        kafkaTemplate.send(KafkaTopicsConfig.USER_EVENTS_TOPIC, key, event);
    }
}
