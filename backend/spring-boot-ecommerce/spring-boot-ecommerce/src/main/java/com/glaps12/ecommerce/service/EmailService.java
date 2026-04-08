package com.glaps12.ecommerce.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String code) {
        System.out.println("\n\n=======================================================");
        System.out.println("📬 SIMULATED EMAIL SENT TO: " + to);
        System.out.println("🔑 YOUR VERIFICATION CODE IS: " + code);
        System.out.println("=======================================================\n\n");
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("BrookyShop - Account Verification");
            message.setText("Welcome to BrookyShop!\n\nPlease use the following code to verify your account: " + code + "\n\nThank you!");
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Note: Actual email dispatch failed (likely missing SMTP config). The code above can be used for testing.");
        }
    }
}
