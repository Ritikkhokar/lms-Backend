package com.example.lmsProject.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmailMessage implements Serializable {

    private String to;
    private String subject;
    private String htmlBody;

    // no-args constructor (required for Jackson)
    public EmailMessage() {}

    public EmailMessage(String to, String subject, String htmlBody) {
        this.to = to;
        this.subject = subject;
        this.htmlBody = htmlBody;
    }

}
