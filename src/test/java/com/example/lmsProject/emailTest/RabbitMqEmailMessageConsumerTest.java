package com.example.lmsProject.emailTest;

import com.example.lmsProject.ServiceImpl.RabbitMqEmailMessageConsumer;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.service.EmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RabbitMqEmailMessageConsumerTest {

    @Test
    void handleEmail_callsEmailServiceWithCorrectParams() {
        EmailService emailService = mock(EmailService.class);
        RabbitMqEmailMessageConsumer consumer = new RabbitMqEmailMessageConsumer(emailService);

        EmailMessage msg = new EmailMessage("ritikkhokar0@gmail.com", "Test", "<p>Hello</p>");

        consumer.handleEmail(msg);

        verify(emailService, times(1))
                .sendGenericEmail("ritikkhokar0@gmail.com", "Test", "<p>Hello</p>");
    }
}