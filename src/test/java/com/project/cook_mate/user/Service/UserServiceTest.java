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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        testUser = User.builder()
                .userId("testId")
                .userPw("testPw")
                .nickName("testNick")
                .email("testEmail")
                .joinDate(LocalDateTime.now())
                .role("USER")
                .recipes(Collections.EMPTY_LIST)
                .build();

//        when(userService.generateNickname()).thenReturn("testNick");

    }

    @Test
    void signUp_Success(){
        when(userCheckService.duplicationId("testId")).thenReturn(false);
        when(userCheckService.duplicationNickName("testNick")).thenReturn(false);
        when(userCheckService.duplicationEmail("testEmail")).thenReturn(false);
        when(bCryptPasswordEncoder.encode("testPw")).thenReturn("encodedPw");
        when(testUserDto.toEntity("encodedPw", "testNick")).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        boolean result = userService.signUp(testUserDto);

        assertTrue(result);
        verify(userRepository, times(1)).save(testUser);

    }

}
