package com.aiworkspace.controller;

import com.aiworkspace.common.ApiResponse;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import com.aiworkspace.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET: Fetch all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();

        ApiResponse<List<UserResponse>> response =
                ApiResponse.<List<UserResponse>>builder()
                        .success(true)
                        .message("Users fetched successfully")
                        .data(users)
                        .timestamp(LocalDateTime.now().toString())
                        .build();

        return ResponseEntity.ok(response);
    }
}