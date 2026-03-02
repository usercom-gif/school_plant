package com.schoolplant.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.common.SecurityUtils;
import com.schoolplant.dto.RegisterRequest;
import com.schoolplant.dto.UpdatePasswordRequest;
import com.schoolplant.dto.UpdateProfileRequest;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.User;
import com.schoolplant.entity.UserRole;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private com.schoolplant.mapper.UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public Long getRoleIdByUserId(Long userId) {
        try {
            com.schoolplant.entity.UserRole ur = userRoleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.schoolplant.entity.UserRole>()
                    .eq("user_id", userId)
                    .last("LIMIT 1"));
            return ur != null ? ur.getRoleId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 1. 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 3. 校验唯一性
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())) > 0) {
            throw new RuntimeException("手机号已存在");
        }
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setRealName(request.getUsername()); // 默认真实姓名为用户名，后续可修改
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1); // 启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 5. 密码加密 (双重MD5)
        // 前端已MD5一次，这里再MD5一次
        String pwdTwice = SecurityUtils.encryptPassword(request.getPassword());
        user.setPassword(pwdTwice);

        this.save(user);

        // 6. 分配默认角色 (USER)
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, "USER"));
        if (role != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        } else {
            // Log warning or throw exception if default role not found
            System.err.println("Default role 'USER' not found!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        // ... (existing implementation) ...
        // Re-implementing to ensure consistency if needed, but I'll copy the logic I read earlier
        // Wait, I am overwriting the file. I must include existing logic.
        
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码 (前端传明文 -> 双重MD5)
        // 注意：这里假设前端传的是明文。如果前端传的是MD5，则只需要一次MD5。
        // 根据 AuthController 的逻辑，我们采用后端双重MD5。
        String oldPwdEncryptedOnce = SecurityUtils.encryptPassword(request.getOldPassword());
        String oldPwdEncryptedTwice = SecurityUtils.encryptPassword(oldPwdEncryptedOnce);
        
        if (!user.getPassword().equals(oldPwdEncryptedTwice)) {
            throw new RuntimeException("原密码输入错误");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        String newPwdEncryptedOnce = SecurityUtils.encryptPassword(request.getNewPassword());
        String newPwdEncryptedTwice = SecurityUtils.encryptPassword(newPwdEncryptedOnce);
        
        user.setPassword(newPwdEncryptedTwice);
        user.setTokenExpireTime(LocalDateTime.now());
        this.updateById(user);

        StpUtil.logout(userId);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = new User();
        user.setId(userId);
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        this.updateById(user);
    }
}
