package com.project.cook_mate.user.service;

import com.project.cook_mate.user.log.LogHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private final LogHelper logHelper;


    public void sendEmail(String Email, int num, String detail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("CookMate@cookmate.com"); // service name
            helper.setTo(Email); // customer email
            if(num == 1) { // 이메일 인증번호
                helper.setSubject("CookMate에서 발송된 인증번호입니다."); // email title
                helper.setText(generateContent(Email), true); // content, html: true
            }
            else if(num == 2) { // 비밀번호 변경 메일
                helper.setSubject("변경된 비밀번호입니다."); // email title
                helper.setText(generateContent2(Email, detail), true); // content, html: true
            }
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logHelper.handleException(e);
            e.printStackTrace(); // 에러 출력
        }
    }

    public int checkEmail(String Email, String code){
        boolean isExpired = isAuthCodeExpired(Email);
        int check = isExpired ? 0 : (getAuthCode(Email).equals(code) ? 1 : 2); //0 = 만료, 1 = 성공, 2 = 실패
        return check;
    }


    public String generateRandomNumber() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // 6자리 숫자 생성
        return String.valueOf(randomNumber);
    }

    public String generateContent(String Email){
        String num = generateRandomNumber();
        saveAuthCode(Email, num);

        String content =
                "<h1 style='font-size: 20px;'>CookMate 인증번호</h1>" +
                        "<p style='font-size: 16px;'>발송된 인증번호: <strong>" + num + "</strong></p>";
        return content;
    }

    public String generateContent2(String Email, String detail){

        String content =
                "<h1 style='font-size: 20px;'>변경된 비밀번호입니다. 로그인 후 변경 부탁드립니다!!</h1>" +
                        "<p style='font-size: 16px;'>비밀번호: <strong>" + detail + "</strong></p>";
        return content;
    }

    // 인증번호 유효시간 확인 (단, 키가 만료되었는지 여부를 체크)
    public boolean isAuthCodeExpired(String email) {
        // "auth:{email}" 키에 대한 남은 시간 확인
        Long ttl = redisTemplate.getExpire("auth:" + email, TimeUnit.SECONDS);

        if (ttl != null && ttl > 0) {
            // ttl이 0보다 크면 인증번호는 유효하다
            return false;
        } else {
            // ttl이 음수거나 0이면 인증번호가 만료되었거나 존재하지 않는다
            return true;
        }
    }

    // 인증 코드 저장
    public void saveAuthCode(String email, String code) {
        String key = "auth:" + email;
//        if(Boolean.TRUE.equals(redisTemplate.hasKey(key))) //없어도 자동 덮어씌우기 가능(다만 로그나 디버깅 측면에서 이부분을 사용가능 함)
//            deleteAuthCode(email);

        redisTemplate.opsForValue().set(key, code, 310, TimeUnit.SECONDS); //5분 인증시간 -> 10초는 저장하고 보내는 동안의 딜레이 시간 고려
    }

    // 인증 코드 가져오기
    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get("auth:" + email);
    }

    // 인증 코드 삭제
    public void deleteAuthCode(String email) {
        redisTemplate.delete("auth:" + email);
    }
}
