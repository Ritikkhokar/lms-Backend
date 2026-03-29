package com.example.lmsProject.service;

import com.example.lmsProject.dto.UserEvent;

public interface UserEventConsumer {
    void handleUserEvent(UserEvent event);
}
