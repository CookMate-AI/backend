package com.project.cook_mate.user.service;

import com.project.cook_mate.user.dto.CustomUserDetails;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService  implements UserDetailsService { //로그인에 필요한 검증 부분 (어디서 하는지 궁금했던 부분)
    private final UserRepository userRepository; //여기 명시로 인하여 해당 인터페이스로 검증
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userData = userRepository.findByuserId(username);

        if(userData != null){
            return new CustomUserDetails(userData);
        }else{
            throw new UsernameNotFoundException("User not found with userId: " + username);
        }

    }
}
