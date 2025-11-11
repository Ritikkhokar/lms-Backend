package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendGradeNotification(
            String to, String assignmentName, String grade, String studentName
    ) throws MessagingException {
        String subject = "Your grade for " + assignmentName + " is uploaded";
        String html = "<h3>Hello " + studentName + ",</h3>"
                + "<p>Your grade for <b>" + assignmentName + "</b> is: <b>" + grade + "</b>.</p>"
                + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    @Override
    public void sendCreateUserNotification(
            String to, String userEmail, String userPassword, String userName
    ) throws MessagingException {
        String subject = "Account created for " + userName;
        String html = "<h3>Hello " + userName + ",</h3>"
                + "<p>Your account for LMS created successfully"
                + "<p>Your email for account is <b>" + userEmail + "</b> and password is  <b>" + userPassword + "</b>.</p>"
                + "<p>Check your dashboard for further details.<br>Regards,<br>LMS Team</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
