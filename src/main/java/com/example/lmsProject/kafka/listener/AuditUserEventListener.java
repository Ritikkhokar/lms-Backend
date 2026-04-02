package com.example.lmsProject.kafka;

import com.example.lmsProject.dto.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditUserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditUserEventListener.class);

    @KafkaListener(
            topics = "user-events",
            groupId = "lms-user-audit-consumer"
    )
    public void onUserEventForAudit(UserEvent event) {
        logger.info("AUDIT listener received user event: type={}, userId={}, email={}",
                event.getEventType(), event.getUserId(), event.getEmail());
        // TODO: later persist into audit_log table
    }
}
