package com.project.cook_mate.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@*";
    private static final String ALL_CHARACTERS = UPPER + LOWER + DIGITS + SPECIALS;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRandomPassword() {
        int length = RANDOM.nextInt(5) + 8; // 8~12자리 랜덤 길이 설정
        StringBuilder password = new StringBuilder();

        // 필수 요소(대문자, 소문자, 숫자, 특수문자) 하나씩 추가
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIALS.charAt(RANDOM.nextInt(SPECIALS.length())));

        // 나머지 랜덤 문자 추가
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
        }

        // 비밀번호를 랜덤하게 섞음
        return shuffleString(password.toString());
    }

    private static String shuffleString(String input) {
        char[] array = input.toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return new String(array);
    }

}

