package com.example.lmsProject.dto;

import java.time.LocalDateTime;

public class UserEvent {

    private String eventType;      // USER_CREATED, USER_UPDATED, USER_DELETED
    private Integer userId;
    private String fullName;
    private String email;
    private String roleName;
    private LocalDateTime occurredAt;

    public UserEvent() {}

    public UserEvent(String eventType,
                     Integer userId,
                     String fullName,
                     String email,
                     String roleName,
                     LocalDateTime occurredAt) {
        this.eventType = eventType;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.roleName = roleName;
        this.occurredAt = occurredAt;
    }

    // getters and setters
}
