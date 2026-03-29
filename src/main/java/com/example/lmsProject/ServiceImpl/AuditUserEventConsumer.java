package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.dto.UserEvent;
import com.example.lmsProject.service.UserEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditUserEventConsumer implements UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuditUserEventConsumer.class);

    @Override
    public void handleUserEvent(UserEvent event) {
        logger.info("AUDIT: User event type={}, userId={}, email={}",
                event.getEventType(), event.getUserId(), event.getEmail());
        // Later: persist to audit table
    }
}
