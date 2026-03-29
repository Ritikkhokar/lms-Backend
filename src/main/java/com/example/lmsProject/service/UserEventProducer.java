package com.example.lmsProject.service;

import com.example.lmsProject.dto.UserEvent;

public interface UserEventProducer {
    void publishUserEvent(UserEvent event);
}
