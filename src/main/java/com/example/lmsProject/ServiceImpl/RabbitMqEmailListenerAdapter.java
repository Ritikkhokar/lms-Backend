package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.config.RabbitMQConfig;
import com.example.lmsProject.dto.EmailMessage;
import com.example.lmsProject.service.EmailMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqEmailListenerAdapter{

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqEmailListenerAdapter.class);

    private final EmailMessageConsumer emailMessageConsumer;

    public RabbitMqEmailListenerAdapter(EmailMessageConsumer emailMessageConsumer) {
        this.emailMessageConsumer = emailMessageConsumer;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void onMessage(EmailMessage message) {
        logger.info("Received email message from RabbitMQ for {}", message.getTo());
        emailMessageConsumer.handleEmail(message);
    }
}

