package com.example.lmsProject.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendGradeNotification(
            String to, String assignmentName, String grade, String studentName
    ) throws MessagingException;
    void sendCreateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) throws MessagingException;
}
