package com.project.cook_mate.user.service;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.dto.UserResponseDto;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Random;


@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final UserCheckService userCheckService;
    private final PasswordGenerator passwordGenerator;

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
            System.out.println(e.getMessage());
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

    public String findPw(String userId, String email){
        Optional<User> optionalUser = userRepository.findByuserIdAndEmail(userId,email);

        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            String pw = passwordGenerator.generateRandomPassword();
            String encodePw = bCryptPasswordEncoder.encode(pw);
            user.setUserPw(encodePw);
            userRepository.save(user);
            return pw; //바뀐 pw 리턴
        }else{
            return "X";
        }


    }

    public Optional<UserResponseDto> loadPersonalInfo(String userId){
        Optional<UserResponseDto> userResponseDto = userRepository.findUserByUserId(userId, 0);
        return userResponseDto;

    }

    public ResponseEntity changePersonalInfo(String userId, String userPw, String nickName, String num){
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("message", "해당하는 개인정보 없음"));
        }
        User user = optionalUser.get();

        if(num.equals("1")){
            user.setNickName(nickName);
            userRepository.save(user);
        } else if (num.equals("2")) {
            String encodePw = bCryptPasswordEncoder.encode(userPw);
            user.setUserPw(encodePw);
            userRepository.save(user);
        }else if (num.equals("3")) {
            user.setNickName(nickName);
            String encodePw = bCryptPasswordEncoder.encode(userPw);
            user.setUserPw(encodePw);
            userRepository.save(user);
        }

        return ResponseEntity.ok().build();


    }

}
