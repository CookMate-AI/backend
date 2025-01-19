package com.project.cook_mate.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String Email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("CookMate@cookmate.com"); // service name
            helper.setTo(Email); // customer email
            helper.setSubject("CookMate에서 발송된 인증번호입니다."); // email title
            helper.setText(generateContent(),true); // content, html: true
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // 에러 출력
        }


    }


    public String generateRandomNumber() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // 6자리 숫자 생성
        return String.valueOf(randomNumber);
    }

    public String generateContent(){
        String num = generateRandomNumber();
        String content =
                "<h1 style='font-size: 20px;'>CookMate 인증번호</h1>" +
                        "<p style='font-size: 16px;'>발송된 인증번호: <strong>" + num + "</strong></p>";
        return content;
    }
}
