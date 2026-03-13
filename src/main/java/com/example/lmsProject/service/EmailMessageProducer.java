package com.example.lmsProject.service;

import com.example.lmsProject.dto.EmailMessage;

public interface EmailMessageProducer {
    void publishEmail(EmailMessage message);
}
