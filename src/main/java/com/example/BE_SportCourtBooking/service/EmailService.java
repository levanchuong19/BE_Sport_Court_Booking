package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.model.Response.EmailDetail;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendEmail(EmailDetail emailDetail) {
        try{
            Context context = new Context();
            context.setVariable("name", emailDetail.getAccount().getFullName());
            context.setVariable("button", "Go to SportZone Platform");
            context.setVariable("link", emailDetail.getLink());
            String template = templateEngine.process("welcome-template", context);
            // Creating a simple mail message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            // Setting up necessary details
            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setText(template, true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            javaMailSender.send(mimeMessage);
        }catch (Exception e) {
            System.out.println("error sent email");
        }

    }

}
