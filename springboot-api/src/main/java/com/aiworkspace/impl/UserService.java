package com.aiworkspace.impl;

import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.mapper.UserMapper;
import com.aiworkspace.repository.UserRepository;
import com.aiworkspace.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
        public List<UserResponse> getAllUsers() {

            return userRepository.findAll()
                    .stream()
                    .map(UserMapper::toResponse)
                    .toList();

        }
    }
