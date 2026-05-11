package com.peredereevin.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final String fromEmail = "peredereev-in@yandex.ru";

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Добро пожаловать!");
        message.setText("Здравствуйте, " + name + "! \n\nСпасибо что выбрали нас! \n\n С уважением, команда от МГТУ им. Н.Э. Баумана");
        mailSender.send(message);
    }
}
