package com.example.lmsProject.service;

import com.example.lmsProject.dto.EmailMessage;

public interface EmailMessageConsumer {

    void handleEmail(EmailMessage message);
}
