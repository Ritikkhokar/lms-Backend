package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.config.RabbitMQConfig;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.service.EmailMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqEmailMessageProducer implements EmailMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqEmailMessageProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public  RabbitMqEmailMessageProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishEmail(EmailMessage message) {
        logger.info("Publishing email to RabbitMQ for {}", message.getTo());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, message);
    }
}
