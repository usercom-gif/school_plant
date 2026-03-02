package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.User;
import com.schoolplant.dto.RegisterRequest;
import com.schoolplant.dto.UpdatePasswordRequest;
import com.schoolplant.dto.UpdateProfileRequest;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    Long getRoleIdByUserId(Long userId);
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void updateProfile(Long userId, UpdateProfileRequest request);
    void register(RegisterRequest request);
}
