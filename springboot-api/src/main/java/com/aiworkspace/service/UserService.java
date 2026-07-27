package com.aiworkspace.service;


import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;

import java.util.List;

public interface UserService {

    public List<UserResponse> getAllUsers();
    //public List<User> getAllUsers();

    public UserResponse register(RegisterRequest request);
}
