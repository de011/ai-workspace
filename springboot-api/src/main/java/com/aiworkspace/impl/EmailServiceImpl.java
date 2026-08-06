package com.aiworkspace.impl;

import com.aiworkspace.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(String to, String firstName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Welcome to AI Workspace");
        message.setText(
                "Hello " + firstName +
                        ",\n\nWelcome to AI Workspace.\n\nHappy Learning!"
        );

        mailSender.send(message);

        log.info("Welcome email sent to {}", to);
    }
}