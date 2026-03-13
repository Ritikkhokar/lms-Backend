package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.config.RabbitMQConfig;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.service.EmailMessageConsumer;
import com.example.lmsProject.service.EmailMessageProducer;
import com.example.lmsProject.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqEmailMessageConsumer implements EmailMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqEmailMessageConsumer.class);

    private final EmailService emailService;

    public RabbitMqEmailMessageConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void handleEmail(EmailMessage message) {
        logger.info("Handling email message for {}", message.getTo());
        emailService.sendGenericEmail(message.getTo(), message.getSubject(), message.getHtmlBody());
    }
}
