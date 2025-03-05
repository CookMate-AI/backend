package com.project.cook_mate.user.Service;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import com.project.cook_mate.user.service.PasswordGenerator;
import com.project.cook_mate.user.service.UserCheckService;
import com.project.cook_mate.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private  PasswordEncoder bCryptPasswordEncoder;
    @Mock
    private  UserRepository userRepository;
    @Mock
    private  UserCheckService userCheckService;
    @Mock
    private  PasswordGenerator passwordGenerator;

    @InjectMocks
    private UserService userService;

    private UserDto testUserDto;
    private User testUser;

    @BeforeEach
    void setUp(){
        testUserDto = new UserDto("testId", "testPw", "testEmail");

    }

    @Test
    void signUp_Success(){
        when(userCheckService.duplicationId("testId")).thenReturn(false);
        when(userCheckService.duplicationEmail("testEmail")).thenReturn(false);
        when(bCryptPasswordEncoder.encode("testPw")).thenReturn("encodedPw");

        UserService spyUserService = Mockito.spy(userService);
        doReturn("testNick").when(spyUserService).generateNickname();
        when(userCheckService.duplicationNickName("testNick")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        boolean result = spyUserService.signUp(testUserDto);

        assertTrue(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("testId", capturedUser.getUserId());
        assertEquals("encodedPw", capturedUser.getUserPw());
        assertEquals("testNick", capturedUser.getNickName());
        assertEquals("testEmail", capturedUser.getEmail());
        assertEquals("user", capturedUser.getRole());

    }

    @Test
    void signUp_DuplicateId_ThrowsException() {
        when(userCheckService.duplicationId("testId")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(testUserDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signUp_DuplicateEmail_ThrowsException() {
        when(userCheckService.duplicationId("testId")).thenReturn(false);
        when(userCheckService.duplicationEmail("testEmail")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.signUp(testUserDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signUp_NicknameGeneration_Retry() {
        when(userCheckService.duplicationId("testId")).thenReturn(false);
        when(userCheckService.duplicationEmail("testEmail")).thenReturn(false);
        when(bCryptPasswordEncoder.encode("testPw")).thenReturn("encodedPw");

        UserService spyUserService = Mockito.spy(userService);
        doReturn("duplicateNick").doReturn("uniqueNick").when(spyUserService).generateNickname();

        when(userCheckService.duplicationNickName("duplicateNick")).thenReturn(true);
        when(userCheckService.duplicationNickName("uniqueNick")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        boolean result = spyUserService.signUp(testUserDto);

        assertTrue(result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("uniqueNick", capturedUser.getNickName());
    }

    @Test
    void findPw_success(){
        String userId = "userId";
        String email = "test@email.com";
        String randomPassword = "newRandomPw";
        String encodedPassword = "encodedRandomPw";

        User mockUser = User.builder()
                .userId(userId)
                .email(email)
                .userPw("oldEncodedPw")
                .build();

        when(userRepository.findByUserIdAndEmailAndSecession(userId,email,0)).thenReturn(Optional.of(mockUser));
        when(passwordGenerator.generateRandomPassword()).thenReturn(randomPassword);
        when(bCryptPasswordEncoder.encode(randomPassword)).thenReturn(encodedPassword);

        String result = userService.findPw(userId,email);

        assertEquals(result, randomPassword);
        assertEquals(encodedPassword, mockUser.getUserPw());

        verify(userRepository).save(mockUser);

    }

    @Test
    void findPw_UserNotFound_ReturnX() {
        // Given
        String userId = "nonExistingId";
        String email = "nonexisting@email.com";

        when(userRepository.findByUserIdAndEmailAndSecession(userId, email, 0))
                .thenReturn(Optional.empty());

        String result = userService.findPw(userId, email);

        assertEquals("X", result);

        // 비밀번호 생성기가 호출되지 않았는지 확인
        verify(passwordGenerator, never()).generateRandomPassword();
        // 저장 메서드가 호출되지 않았는지 확인
        verify(userRepository, never()).save(any(User.class));
    }

}
