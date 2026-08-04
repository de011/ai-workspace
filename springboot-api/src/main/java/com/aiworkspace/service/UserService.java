package com.aiworkspace.service;


import com.aiworkspace.dto.LoginRequest;
import com.aiworkspace.dto.LoginResponse;
import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);
    public List<UserResponse> getAllUser();
    //public List<User> getAllUsers();

    public UserResponse register(RegisterRequest request);

    public LoginResponse login(LoginRequest request);
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);
}
