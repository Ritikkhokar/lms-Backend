package com.example.lmsProject.phase1;

import com.example.lmsProject.service.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Async Email Tests
 */
@SpringBootTest(classes = com.example.lmsProject.LmsProjectApplication.class)
@DisplayName("Async Email Tests")
class AsyncEmailTest {

    @Autowired
    private EmailService emailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Email sending is asynchronous and returns immediately")
    void testAsyncEmailSending() throws MessagingException {
        String testEmail = "ritikkhokar0@gmail.com";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> future = emailService.sendGradeNotification(
            testEmail, "Assignment 1", "A", "John Doe"
        );
        long callDuration = System.currentTimeMillis() - startTime;

        assertNotNull(future);
        assertTrue(callDuration < 100);
        System.out.println("✅ Async email returned in: " + callDuration + "ms");
    }

    @Test
    @DisplayName("Multiple emails can be sent concurrently")
    void testConcurrentEmails() throws Exception {
        int emailCount = 5;
        CompletableFuture<?>[] futures = new CompletableFuture[emailCount];

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < emailCount; i++) {
            futures[i] = emailService.sendGradeNotification(
                "student" + i + "@example.com", "Assign " + i, "A", "Student " + i
            );
        }
        long totalTime = System.currentTimeMillis() - startTime;

        assertTrue(totalTime < 500);
        System.out.println("✅ " + emailCount + " concurrent emails in: " + totalTime + "ms");

        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
    }
}

