package com.example.lmsProject.emailTest;

import com.example.lmsProject.Repository.DeadLetterEmailRepository;
import com.example.lmsProject.ServiceImpl.EmailServiceImpl;
import com.example.lmsProject.ServiceImpl.RabbitMqEmailMessageProducer;
import com.example.lmsProject.entity.DeadLetterEmail;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.support.RetryTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceImplRetryTest {

    @Test
    void sendGenericEmail_success_noDLQ() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DeadLetterEmailRepository deadLetterRepo = mock(DeadLetterEmailRepository.class);
        RabbitMqEmailMessageProducer producer = mock(RabbitMqEmailMessageProducer.class);

        RetryTemplate retryTemplate = new RetryTemplate();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // MimeMessageHelper uses the message; we can ignore its internals here

        EmailServiceImpl service = new EmailServiceImpl(mailSender, deadLetterRepo, producer);
        service.emailRetryTemplate = retryTemplate;

        service.sendGenericEmail("ritikkhokar0@gmail.com", "Subj", "<p>Body</p>");

        verify(mailSender, times(1)).send(mimeMessage);
        verifyNoInteractions(deadLetterRepo);
    }

    @Test
    void sendGenericEmail_fails_afterRetries_savesToDeadLetter() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DeadLetterEmailRepository deadLetterRepo = mock(DeadLetterEmailRepository.class);
        RabbitMqEmailMessageProducer producer = mock(RabbitMqEmailMessageProducer.class);

        RetryTemplate retryTemplate = new RetryTemplate();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MessagingException("SMTP down"))
                .when(mailSender).send(mimeMessage);

        EmailServiceImpl service = new EmailServiceImpl(mailSender, deadLetterRepo, producer);
        service.emailRetryTemplate = retryTemplate;

        service.sendGenericEmail("ritikkhokar0@gmail.com", "Subj", "<p>Body</p>");

        // Verify DLQ was written
        ArgumentCaptor<DeadLetterEmail> dlqCaptor = ArgumentCaptor.forClass(DeadLetterEmail.class);
        verify(deadLetterRepo, times(1)).save(dlqCaptor.capture());

        DeadLetterEmail dle = dlqCaptor.getValue();
        assertThat(dle.getRecipient()).isEqualTo("ritikkhokar0@gmail.com");
        assertThat(dle.getSubject()).isEqualTo("Subj");
        assertThat(dle.getBody()).contains("Body");
        assertThat(dle.getReason()).contains("MessagingException");
    }

}
