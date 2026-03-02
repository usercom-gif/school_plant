package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.schoolplant.common.R;
import com.schoolplant.dto.UserProfileResponse;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.service.UserService;
import com.schoolplant.service.PlantAbnormalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PlantAbnormalityService abnormalityService;

    @GetMapping("/profile")
    @SaCheckLogin
    public R<UserProfileResponse> getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        
        if (user == null) {
            return R.fail("用户不存在");
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setAccount(user.getUsername());
        response.setName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatarUrl(user.getAvatarUrl());
        
        if (user.getCreatedAt() != null) {
            response.setRegisterTime(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // Get Role
        Long roleId = userService.getRoleIdByUserId(userId);
        String roleKey = "USER";
        String roleName = "普通用户";
        
        if (roleId != null) {
            Role role = roleMapper.selectById(roleId);
            if (role != null) {
                roleKey = role.getRoleKey();
                roleName = role.getRoleName();
            }
        }
        
        response.setRole(roleName);
        response.setIdNumber(user.getStudentId() != null ? user.getStudentId() : "暂无");

        // Calculate Statistics based on Role
        int statNum = 0;
        if ("ADMIN".equals(roleKey)) {
            // Mock: Count audits (In real app, query audit table)
            statNum = 5; 
        } else if ("MAINTAINER".equals(roleKey)) {
             // Count resolved abnormalities
             statNum = abnormalityService.countResolvedByMaintainer(userId);
        } else {
            // USER: Count adoptions (Mock for now as Adoption table is not fully implemented in this context)
            statNum = 0; 
        }
        response.setStatisticNum(statNum);

        return R.ok(response);
    }

    @org.springframework.web.bind.annotation.PutMapping("/profile")
    @SaCheckLogin
    public R<Void> updateProfile(@org.springframework.web.bind.annotation.RequestBody com.schoolplant.dto.UpdateProfileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, request);
        return R.ok();
    }

    @org.springframework.web.bind.annotation.PutMapping("/password")
    @SaCheckLogin
    public R<Void> updatePassword(@org.springframework.web.bind.annotation.RequestBody com.schoolplant.dto.UpdatePasswordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updatePassword(userId, request);
        return R.ok();
    }

}
