package com.example.lmsProject.service;

import jakarta.mail.MessagingException;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<Void> sendGradeNotification(
            String to, String assignmentName, String grade, String studentName
    ) throws MessagingException;
    CompletableFuture<Void> sendCreateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) throws MessagingException;
}
