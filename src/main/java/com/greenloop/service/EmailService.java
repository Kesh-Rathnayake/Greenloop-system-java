package com.greenloop.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private final Properties config = new Properties();

    public EmailService() {
        try (InputStream input = EmailService.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties file not found.");
            }

            config.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Error loading email configuration: " + e.getMessage());
        }
    }

    public void sendEmail(String toEmail, String subject, String messageText) {
        String host = config.getProperty("mail.host");
        String port = config.getProperty("mail.port");
        String username = config.getProperty("mail.username");
        String password = config.getProperty("mail.password");

        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.host", host);
        mailProperties.put("mail.smtp.port", port);

        Session session = Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(username));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            emailMessage.setSubject(subject);
            emailMessage.setText(messageText);

            Transport.send(emailMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }
}