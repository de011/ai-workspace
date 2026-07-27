package com.aiworkspace.impl;

import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import com.aiworkspace.mapper.UserMapper;
import com.aiworkspace.repository.UserRepository;
import com.aiworkspace.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
        public List<UserResponse> getAllUsers() {

            return userRepository.findAll()
                    .stream()
                    .map(UserMapper::toResponse)
                    .toList();

        }

    @Override
    public UserResponse register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))   // Encrypt the password
                .role("USER")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }
}
