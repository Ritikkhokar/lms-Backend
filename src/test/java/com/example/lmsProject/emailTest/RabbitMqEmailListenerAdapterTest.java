package com.example.lmsProject.emailTest;

import com.example.lmsProject.ServiceImpl.RabbitMqEmailListenerAdapter;
import com.example.lmsProject.config.RabbitMQConfig;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.service.EmailMessageConsumer;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RabbitMqEmailListenerAdapterTest {

    @Test
    void onMessage_delegatesToConsumer() {
        EmailMessageConsumer consumer = mock(EmailMessageConsumer.class);
        RabbitMqEmailListenerAdapter adapter = new RabbitMqEmailListenerAdapter(consumer);

        EmailMessage msg = new EmailMessage("ritikkhokar0@gmail.com", "Test", "<p>Hi</p>");

        adapter.onMessage(msg);

        verify(consumer, times(1)).handleEmail(msg);
    }
}