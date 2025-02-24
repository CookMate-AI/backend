package com.project.cook_mate.user.controller;

import com.project.cook_mate.jwt.JWTUtil;
import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.dto.UserResponseDto;
import com.project.cook_mate.user.service.AuthService;
import com.project.cook_mate.user.service.MailService;
import com.project.cook_mate.user.service.UserCheckService;
import com.project.cook_mate.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserCheckService userCheckService;
    private final MailService mailService;
    private final UserService userService;
    private final AuthService authService;
    private final JWTUtil jwtUtil;

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        System.out.println("test메서드 들어옴");
        return ResponseEntity.ok(Map.of("message", "인증성공"));
    }

    @GetMapping("/check-id")
    public ResponseEntity<?> checkId(@RequestParam String userId){
//        String userId = (String) requestData.get("userId");
        System.out.println(userId);
        boolean isExist = userCheckService.duplicationId(userId);

        if(isExist){
            return ResponseEntity.ok(Map.of("message", "해당 ID가 이미 존재합니다", "isExist", true));
        }else{
            return ResponseEntity.ok(Map.of("message", "해당 ID는 사용가능합니다", "isExist", false));
        }
    }

    @PostMapping("/check-Email/send-Email")
    public ResponseEntity<?> certificationNumber(@RequestParam String email){
//        String email = (String) requestData.get("email");
        boolean isExist = userCheckService.duplicationEmail(email);

        if(isExist){
            return ResponseEntity.ok(Map.of("message", "해당 Email이 이미 존재합니다.", "isExist", true));
        }else{

            mailService.sendEmail(email, 1, "");

            return ResponseEntity.ok(Map.of("message", "보내신 이메일 주소로 인증번호가 발송되었습니다.", "isExist", false));
        }
    }

    @PostMapping("/check-Email/certification")
    public ResponseEntity<?> checkCertificationNumber(@RequestBody Map<String, Object> requestData){
        String email = (String) requestData.get("email");
        String code = (String) requestData.get("code");

        int check = mailService.checkEmail(email, code);

        if(check == 0){
            return ResponseEntity.ok(Map.of("message", "인증번호가 만료되었습니다.", "checkNum", 0));
        }else if(check ==1){
            mailService.deleteAuthCode(email);
            return ResponseEntity.ok(Map.of("message", "인증번호가 인증성공", "checkNum", 1));
        }else{
            return ResponseEntity.ok(Map.of("message", "인증번호가 일치하지 않습니다.", "checkNum", 2));
        }
    }

    @GetMapping("/check-Nname")
    public ResponseEntity<?> checkNickName(@RequestParam String nickName){
//        String nickName = (String) requestData.get("nickName");
        boolean isExist = userCheckService.duplicationNickName(nickName);

        if(isExist){
            return ResponseEntity.ok(Map.of("message", "해당 닉네임이 이미 존재합니다", "isExist", true));
        }else{
            return ResponseEntity.ok(Map.of("message", "해당 닉네임은 사용가능합니다", "isExist", false));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto userDto){
        try {
            boolean result = userService.signUp(userDto);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED) // 201 Created
                        .body("회원가입이 성공적으로 완료되었습니다.");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("회원가입에 실패했습니다.");
        }catch (IllegalArgumentException e) {
            // 유효성 검사 실패 시 (예: 중복된 아이디나 이메일)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            // 서버 측 문제 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body("서버 오류가 발생했습니다. 다시 시도해주세요.");
        }

    }

    @PostMapping("/find-id/send-Email")
    public ResponseEntity<?> findId(@RequestBody Map<String, Object> requestData){
        String email = (String) requestData.get("email");
        boolean isExist = userCheckService.duplicationEmail(email);

        if(isExist){
            mailService.sendEmail(email, 1, "");
            return ResponseEntity.ok(Map.of("message", "해당 Email주소로 전송이 완료 되었습니다."));
        }else{

            return ResponseEntity.ok(Map.of("message", "해당 Email은 가입한 이력이 없습니다."));
        }
    }

    @PostMapping("/find-id/certification")
    public ResponseEntity<?> checkFindIdCertificationNumber(@RequestBody Map<String, Object> requestData){
        String code = (String) requestData.get("code");
        String email = (String) requestData.get("email");

        int check = mailService.checkEmail(email, code);

        if(check == 0){
            return ResponseEntity.ok(Map.of("message", "인증번호가 만료되었습니다.", "checkNum", 0));
        }else if(check ==1){
            String id = userCheckService.returnId(email);
            return ResponseEntity.ok(Map.of("message", "인증번호가 인증성공", "ID", id));
        }else{
            return ResponseEntity.ok(Map.of("message", "인증번호가 일치하지 않습니다.", "checkNum", 1));
        }
    }

    @PostMapping("/find-pw")
    public ResponseEntity<?> checkFindPw(@RequestBody Map<String, Object> requestData){
        String userId = (String) requestData.get("userId");
        String email = (String) requestData.get("email");

        String result = userService.findPw(userId,email);

        if(result.equals("X")){
            return ResponseEntity.ok(Map.of("message", "Id 와 email이 일치하지 않습니다"));
        }else{
            System.out.println(result);
            mailService.sendEmail(email, 2, result);
            return ResponseEntity.ok(Map.of("message", "이메일로 변경된 비밀번호가 발송되었습니다. 반드시 비밀번호를 변경해주세요."));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> loadInformation(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        Optional<UserResponseDto> user = userService.loadPersonalInfo(customUserDetails.getUsername());

        if(user.isPresent()){
            return ResponseEntity.ok(user);
        }else{
            return ResponseEntity.status(404).body(Map.of("message", "개인정보를 불러오지 못함"));
        }
    }

    @PutMapping("/info")
    public ResponseEntity<?> editInformation(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                             @RequestBody Map<String, Object> requestData){
        String id = customUserDetails.getUsername();
        ResponseEntity response = userService.changePersonalInfo(id, requestData);

        return response;
    }

    @DeleteMapping("/secession")
    public ResponseEntity<?> secessionUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        String id = customUserDetails.getUsername();
        ResponseEntity response = userService.deleteUser(id);

        return response;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"토큰이 필요합니다.\"}");
        }

        // 토큰 남은 시간 확인 후 Redis에 추가
        long expiration = jwtUtil.getExpirationTime(token);
        authService.addToBlacklist(token, expiration);

        return ResponseEntity.ok("{\"message\": \"로그아웃 성공\"}");
    }

}
