package com.project.cook_mate.user.service;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;


@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final UserCheckService userCheckService;

    public boolean signUp(UserDto userDto){

        if (userCheckService.duplicationId(userDto.getUserId())) {
            throw new IllegalArgumentException("아이디가 이미 존재합니다.");
        }
        if (userCheckService.duplicationEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다.");
        }

        String encodePw = bCryptPasswordEncoder.encode(userDto.getUserPw());

        String nickName = generateNickname();
        while (userCheckService.duplicationNickName(nickName)){
            nickName = generateNickname();
        }

        User user = userDto.toEntity(encodePw, nickName);
        try {
            userRepository.save(user);
            return true;  // 성공적으로 가입 처리됨
        } catch (Exception e) {
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다.");
        }


    }

    private String generateNickname(){
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            char letter = letters.charAt(random.nextInt(letters.length()));
            sb.append(letter);
        }

        int number = 10000 + random.nextInt(90000); // 10000 ~ 99999 범위의 숫자 생성
        sb.append(number);

        return sb.toString();
    }
}
