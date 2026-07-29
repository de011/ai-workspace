package com.aiworkspace.impl;

import com.aiworkspace.dto.LoginRequest;
import com.aiworkspace.dto.LoginResponse;
import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import com.aiworkspace.mapper.UserMapper;
import com.aiworkspace.repository.UserRepository;
import com.aiworkspace.security.JwtService;
import com.aiworkspace.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtService jwtService1) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService1;
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

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid Email or Password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid Email or Password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(UserMapper.toResponse(user))
                .build();
    }
}
