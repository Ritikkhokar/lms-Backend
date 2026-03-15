package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.Repository.DeadLetterEmailRepository;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.entity.DeadLetterEmail;
import com.example.lmsProject.service.EmailMessageConsumer;
import com.example.lmsProject.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.CompletableFuture;

/**
 * Email Service Implementation with Centralized Retry Logic
 *
 * Retry mechanism:
 * - Uses RetryTemplate from RetryConfig (centralized)
 * - Retries 3 times with exponential backoff (2s → 4s → 8s)
 * - Failed emails are saved to DeadLetterEmail table
 * - Can be reused in Phase 2 for RabbitMQ message handling
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final DeadLetterEmailRepository deadLetterEmailRepository;

    // ===== CENTRALIZED RETRY TEMPLATE =====
    // Injected from RetryConfig
    // Contains retry policy: 3 attempts, 2s→4s→8s backoff
    // Can be easily changed in one place (RetryConfig)
    @Autowired
    @Qualifier("emailRetryTemplate")
    private RetryTemplate emailRetryTemplate;
    private RabbitMqEmailMessageProducer rabbitMqEmailMessageProducer;
    // ...existing code...

    public EmailServiceImpl(JavaMailSender mailSender, DeadLetterEmailRepository deadLetterEmailRepository, RabbitMqEmailMessageProducer rabbitMqEmailMessageProducer) {
        this.mailSender = mailSender;
        this.deadLetterEmailRepository = deadLetterEmailRepository;
        this.rabbitMqEmailMessageProducer = rabbitMqEmailMessageProducer;
    }

    // ===== NOTIFICATION METHODS =====
    // All use the same retry logic via emailRetryTemplate
    // No hardcoded retry parameters

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendGradeNotification(
            String to, String assignmentName, String grade, String studentName
    ) {
        String subject = "Your grade for " + assignmentName + " is uploaded";
        String html = "<h3>Hello " + studentName + ",</h3>"
                + "<p>Your grade for <b>" + assignmentName + "</b> is: <b>" + grade + "</b>.</p>"
                + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

        // Uses emailRetryTemplate from RetryConfig (3 attempts with backoff)
        rabbitMqEmailMessageProducer.publishEmail(new EmailMessage(to, subject, html));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendCreateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) {
        String subject = "Account created for " + userName;
        String html = "<h3>Hello " + userName + ",</h3>"
                + "<p>Your account for LMS created successfully"
                + "<p>Your email for account is <b>" + userEmail + "</b> and password is <b>" + userPassword + "</b>.</p>"
                + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

        // Uses emailRetryTemplate from RetryConfig (3 attempts with backoff)
        rabbitMqEmailMessageProducer.publishEmail(new EmailMessage(to, subject, html));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendUpdateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) {
        String subject = "Account updated for " + userName;
        String html = "<h3>Hello " + userName + ",</h3>"
                + "<p>Your account for LMS updated successfully"
                + "<p>Your email for account is <b>" + userEmail + "</b> and password is <b>" + userPassword + "</b>.</p>"
                + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

        // Uses emailRetryTemplate from RetryConfig (3 attempts with backoff)
        rabbitMqEmailMessageProducer.publishEmail(new EmailMessage(to, subject, html));
        return CompletableFuture.completedFuture(null);
    }

    // ===== CORE EMAIL SENDING WITH CENTRALIZED RETRY =====
    // This method uses RetryTemplate from RetryConfig instead of @Retryable
    // How it works:
    // 1. emailRetryTemplate is injected from RetryConfig
    // 2. Retry logic is defined once in RetryConfig.java
    // 3. Retry policy: 3 attempts, 2s→4s→8s exponential backoff
    // 4. On success: Email sent ✅
    // 5. On failure (after 3 attempts): Goes to recoverFromEmailFailure()

    private void sendEmailWithCentralizedRetry(String to, String subject, String html) {
        try {
            // Use RetryTemplate.execute() to handle retries
            // RetryTemplate will retry automatically up to 3 times
            // with exponential backoff defined in RetryConfig
            emailRetryTemplate.execute(context -> {
                logger.info("📧 Sending email attempt #{} to {} with subject '{}'",
                    context.getRetryCount() + 1, to, subject);

                try {
                    sendActualEmail(to, subject, html);
                    logger.info("✅ Successfully sent email to {}", to);
                    return null;
                } catch (Exception e) {
                    logger.warn("⚠️ Email send failed (attempt #{}): {}",
                        context.getRetryCount() + 1, e.getMessage());
                    throw e; // Let RetryTemplate handle retry
                }
            });

        } catch (Exception ex) {
            // This exception is thrown after ALL retries are exhausted
            // Move to dead letter queue for manual intervention
            logger.error("❌ Email failed after all retries. Recipient: {}, reason: {}",
                to, ex.getMessage(), ex);

            saveToDeadLetterQueue(to, subject, html, ex);
        }
    }

    // ===== ACTUAL EMAIL SENDING LOGIC =====
    // This is the core SMTP logic (no retry here)
    // Retry is handled by emailRetryTemplate above

    private void sendActualEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }

    // ===== DEAD LETTER QUEUE HANDLER =====
    // Called when email fails after all retries exhausted

    private void saveToDeadLetterQueue(String to, String subject, String html, Exception ex) {
        DeadLetterEmail dle = new DeadLetterEmail();
        dle.setRecipient(to);
        dle.setSubject(subject);
        dle.setBody(html);
        dle.setReason(ex.getClass().getSimpleName() + ": " + ex.getMessage());

        deadLetterEmailRepository.save(dle);

        logger.error("📌 Email moved to DeadLetterQueue for manual intervention. Recipient: {}", to);
    }

    @Override
    public void sendGenericEmail(String to, String subject, String html) {
        sendEmailWithCentralizedRetry(to, subject, html);
    }
}
