package com.example.aquaticboogaloo.service;

import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.exception.ResourceNotFoundException;
import com.example.aquaticboogaloo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.aquaticboogaloo.util.EntityConst.ID;
import static com.example.aquaticboogaloo.util.EntityConst.USER;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(ID, USER, id));
    }
}
