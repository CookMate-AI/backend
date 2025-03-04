package com.project.cook_mate.user.service;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.dto.UserResponseDto;
import com.project.cook_mate.user.log.LogHelper;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final LogHelper logHelper;

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

    public String generateNickname(){
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
        Optional<User> optionalUser = userRepository.findByUserIdAndEmailAndSecession(userId,email, 0);

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

    public ResponseEntity changePersonalInfo(String userId, Map<String, Object> requestData){
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("message", "해당하는 개인정보 없음"));
        }
        User user = optionalUser.get();
        int num = (Integer) requestData.get("num");

        if(num==1){
            String nickName = (String) requestData.get("nickName");
            user.setNickName(nickName);
            user.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        } else if (num==2) {
            String userPw = (String) requestData.get("userPw");
            String encodePw = bCryptPasswordEncoder.encode(userPw);
            user.setUserPw(encodePw);
            user.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        }else if (num==3) {
            String nickName = (String) requestData.get("nickName");
            String userPw = (String) requestData.get("userPw");
            user.setNickName(nickName);
            String encodePw = bCryptPasswordEncoder.encode(userPw);
            user.setUserPw(encodePw);
            user.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        }

        return ResponseEntity.ok().build();

    }

    public ResponseEntity deleteUser(String userId){
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("message", "해당하는 개인정보 없음"));
        }
        try {
            User user = optionalUser.get();
            user.setSecession(1);
            user.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        }catch (Exception e){
            logHelper.handleException(e);
            return ResponseEntity.internalServerError().body(Map.of("message", "데이터베이스 에러"));
        }


        return ResponseEntity.ok().build();

    }

}
