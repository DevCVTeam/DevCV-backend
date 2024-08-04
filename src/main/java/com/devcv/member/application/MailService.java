package com.devcv.member.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Service
public class MailService {

    private final JavaMailSender javamailSender;

    public MailService(JavaMailSender javamailSender) {
        this.javamailSender = javamailSender;
    }
    private static final String senderEmail = "devcv0712@gmail.com";
    private static Long number;

    public static void createNumber() {
        number = (long) (Math.random() * (90000)) + 100000;
    }

    public MimeMessage CreateMail(String mail) throws UnsupportedEncodingException{
        createNumber();
        MimeMessage message = javamailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress(senderEmail, "DEVCV"));
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다" + "</h3>";
            body += "<h1>" + number + "</h1>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.fillInStackTrace();
        }

        return message;

    }

    public Long sendMail(String mail) throws UnsupportedEncodingException {

        MimeMessage message = CreateMail(mail);

        Objects.requireNonNull(javamailSender).send(message);

        return number;
    }
}
