package com.project.cook_mate.user.service;

import com.project.cook_mate.user.dto.UserDto;
import com.project.cook_mate.user.model.User;
import com.project.cook_mate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserCheckService {
    private final UserRepository userRepository;

    public boolean duplicationId(String Id){
        return userRepository.existsById(Id);
    }

    public boolean duplicationEmail(String email){
        return userRepository.existsByEmailAndSecession(email, 0);
    }

    public boolean duplicationNickName(String nickName){
        return userRepository.existsByNickNameAndSecession(nickName, 0);
    }

    public String returnId(String email){
        Optional<UserDto> user = userRepository.findUserDtoByUserEmail(email, 0);
        return user.get().getUserId();
    }

}
