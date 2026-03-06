package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.DeadLetterEmailRepository;
import com.example.lmsProject.entity.DeadLetterEmail;
import com.example.lmsProject.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final DeadLetterEmailRepository deadLetterEmailRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(JavaMailSender mailSender, DeadLetterEmailRepository deadLetterEmailRepository) {
        this.mailSender = mailSender;
        this.deadLetterEmailRepository = deadLetterEmailRepository;
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendGradeNotification(
            String to, String assignmentName, String grade, String studentName
    ) throws MessagingException {
            String subject = "Your grade for " + assignmentName + " is uploaded";
            String html = "<h3>Hello " + studentName + ",</h3>"
                    + "<p>Your grade for <b>" + assignmentName + "</b> is: <b>" + grade + "</b>.</p>"
                    + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

            sendEmailWithRetry(to, subject, html);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendCreateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) throws MessagingException {
            String subject = "Account created for " + userName;
            String html = "<h3>Hello " + userName + ",</h3>"
                    + "<p>Your account for LMS created successfully"
                    + "<p>Your email for account is <b>" + userEmail + "</b> and password is  <b>" + userPassword + "</b>.</p>"
                    + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

            sendEmailWithRetry(to, subject, html);  // this method is retryable
        return CompletableFuture.completedFuture(null);
    }


    // Core send logic with retry
    @Retryable(
            retryFor = {MailException.class, MessagingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendEmailWithRetry(String to, String subject, String html)
            throws MessagingException {

        logger.info("Sending email to {} with subject '{}'", to, subject);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);

        logger.info("Successfully sent email to {}", to);
    }

    // Called after all retries fail
    @Recover
    public void recoverFromEmailFailure(Exception ex, String to, String subject, String html) {
        logger.error("Email failed after retries. Moving to dead-letter. Recipient: {}, reason: {}",
                to, ex.getMessage(), ex);

        DeadLetterEmail dle = new DeadLetterEmail();
        dle.setRecipient(to);
        dle.setSubject(subject);
        dle.setBody(html);
        dle.setReason(ex.getClass().getSimpleName() + ": " + ex.getMessage());

        deadLetterEmailRepository.save(dle);
    }
}

