package com.example.lmsProject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class UserAnalytics {

    @Id
    private Integer userId;

    private String email;
    private String fullName;
    private String roleName;

    private String lastEventType;
    private LocalDateTime lastEventTime;
    private boolean active;

    public UserAnalytics() {}

    public UserAnalytics(Integer userId) {
        this.userId = userId;
    }


}
