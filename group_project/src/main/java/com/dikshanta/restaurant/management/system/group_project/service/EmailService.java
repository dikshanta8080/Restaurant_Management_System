//package com.dikshanta.restaurant.management.system.group_project.service;
//
//import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.MailException;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//    private final JavaMailSender javaMailSender;
//
//    public void sendEmail(User user) throws MailException, MessagingException {
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
//        helper.setTo("np05cp4a240203@iic.edu.np");
//        helper.setFrom("dikshantaacharya04@gmail.com");
//        helper.setText("Notification about signup!");
//        javaMailSender.send(mimeMessage);
//    }
//}
