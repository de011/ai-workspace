package com.aiworkspace.impl;

import com.aiworkspace.dto.LoginRequest;
import com.aiworkspace.dto.LoginResponse;
import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import com.aiworkspace.exception.DuplicateResourceException;
import com.aiworkspace.exception.UnauthorizedException;
import com.aiworkspace.mapper.UserMapper;
import com.aiworkspace.repository.UserRepository;
import com.aiworkspace.security.JwtService;
import com.aiworkspace.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public List<UserResponse> getAllUser() {

        log.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        log.info("Total users fetched: {}", users.size());

        return users;
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        log.info("Fetching users. Page: {}, Size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        log.info("Total Records: {}", users.getTotalElements());

        return users.map(UserMapper::toResponse);
    }

    @Override
    public UserResponse register(RegisterRequest request) {

        log.info("Registration request received for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {

            log.warn("Registration failed. Email already exists: {}", request.getEmail());

            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully. User ID: {}", savedUser.getId());

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login request received for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {

                    log.warn("Login failed. Email not found: {}", request.getEmail());

                    return new UnauthorizedException("Invalid Email or Password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Login failed. Invalid password for email: {}", request.getEmail());

            throw new UnauthorizedException("Invalid Email or Password");
        }

        String token = jwtService.generateToken(user.getEmail());

        log.info("Login successful for email: {}", request.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(UserMapper.toResponse(user))
                .build();
    }

   /* @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, keyword, pageable);
        return users.map(UserMapper::toResponse);
    }*/
    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {

        log.info("Searching users with keyword: {}", keyword);

        Page<User> users =
                userRepository
                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                keyword,
                                keyword,
                                keyword,
                                pageable);

        log.info("Total users found: {}", users.getTotalElements());

        return users.map(UserMapper::toResponse);
    }
}