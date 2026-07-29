package com.aiworkspace.service;


import com.aiworkspace.dto.LoginRequest;
import com.aiworkspace.dto.LoginResponse;
import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;

import java.util.List;

public interface UserService {

    public List<UserResponse> getAllUsers();
    //public List<User> getAllUsers();

    public UserResponse register(RegisterRequest request);

    public LoginResponse login(LoginRequest request);
}
