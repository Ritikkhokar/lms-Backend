package com.example.lmsProject.emailTest;

import com.example.lmsProject.ServiceImpl.RabbitMqEmailMessageProducer;
import com.example.lmsProject.config.RabbitMQConfig;
import com.example.lmsProject.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

class RabbitMqEmailMessageProducerTest {

    @Test
    void publishEmail_sendsToConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMqEmailMessageProducer producer = new RabbitMqEmailMessageProducer(rabbitTemplate);

        EmailMessage msg = new EmailMessage("ritikkhokar0@gmail.com", "Subject", "<p>Body</p>");

        producer.publishEmail(msg);

        verify(rabbitTemplate, times(1))
                .convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE,
                        RabbitMQConfig.EMAIL_ROUTING_KEY,
                        msg);
    }
}