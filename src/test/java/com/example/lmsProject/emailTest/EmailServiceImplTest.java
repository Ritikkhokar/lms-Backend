package com.example.lmsProject.emailTest;

import com.example.lmsProject.Repository.DeadLetterEmailRepository;
import com.example.lmsProject.ServiceImpl.EmailServiceImpl;
import com.example.lmsProject.ServiceImpl.RabbitMqEmailMessageProducer;
import com.example.lmsProject.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Test
    void sendCreateUserNotification_publishesMessageToRabbitMQ() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DeadLetterEmailRepository deadLetterRepo = mock(DeadLetterEmailRepository.class);
        RetryTemplate retryTemplate = mock(RetryTemplate.class);
        RabbitMqEmailMessageProducer producer = mock(RabbitMqEmailMessageProducer.class);

        EmailServiceImpl service = new EmailServiceImpl(mailSender, deadLetterRepo, producer);
        service.emailRetryTemplate = retryTemplate;

        String to = "ritikkhokar0@gmail.com";
        String email = "ritikkhokar00@gmail.com";
        String pwd = "pwd123";
        String name = "John Doe";

        service.sendCreateUserNotification(to, email, pwd, name);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(producer, times(1)).publishEmail(captor.capture());

        EmailMessage msg = captor.getValue();
        assertThat(msg.getTo()).isEqualTo(to);
        assertThat(msg.getSubject()).contains("Account created for " + name);
        assertThat(msg.getHtmlBody()).contains(email).contains(pwd);
    }

    @Test
    void sendGradeNotification_publishesMessageToRabbitMQ() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DeadLetterEmailRepository deadLetterRepo = mock(DeadLetterEmailRepository.class);
        RetryTemplate retryTemplate = mock(RetryTemplate.class);
        RabbitMqEmailMessageProducer producer = mock(RabbitMqEmailMessageProducer.class);

        EmailServiceImpl service = new EmailServiceImpl(mailSender, deadLetterRepo, producer);
        service.emailRetryTemplate = retryTemplate;

        service.sendGradeNotification("ritikkhokar0@gmail.com", "Assignment 1", "95", "Ritik");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(producer).publishEmail(captor.capture());

        EmailMessage msg = captor.getValue();
        assertThat(msg.getTo()).isEqualTo("ritikkhokar0@gmail.com");
        assertThat(msg.getSubject()).contains("Assignment 1");
        assertThat(msg.getHtmlBody()).contains("95").contains("Ritik");
    }

    @Test
    void sendUpdateUserNotification_publishesMessageToRabbitMQ() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DeadLetterEmailRepository deadLetterRepo = mock(DeadLetterEmailRepository.class);
        RetryTemplate retryTemplate = mock(RetryTemplate.class);
        RabbitMqEmailMessageProducer producer = mock(RabbitMqEmailMessageProducer.class);

        EmailServiceImpl service = new EmailServiceImpl(mailSender, deadLetterRepo, producer);
        service.emailRetryTemplate = retryTemplate;

        service.sendUpdateUserNotification("ritikkhokar0@gmail.com", "ritikkhokar0@gmail.com", "newPwd", "Ritik");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(producer).publishEmail(captor.capture());

        EmailMessage msg = captor.getValue();
        assertThat(msg.getSubject()).contains("Account updated for Ritik");
        assertThat(msg.getHtmlBody()).contains("newPwd");
    }
}
