package com.schoolplant.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.schoolplant.common.R;
import com.schoolplant.common.SecurityUtils;
import com.schoolplant.dto.LoginRequest;
import com.schoolplant.dto.RegisterRequest;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.mapper.UserRoleMapper;
import com.schoolplant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        try {
            // 1. 密码二次加密比对 (前端MD5 -> 后端MD5)
            String encryptedPwd = SecurityUtils.encryptPassword(request.getPassword());
            
            User user = userService.getByUsername(request.getUsername());
            if (user == null) {
                return R.fail("用户名或密码错误");
            }
            
            if (!user.getPassword().equals(encryptedPwd)) {
                return R.fail("用户名或密码错误");
            }
            
            if (user.getStatus() != null && user.getStatus() == 0) {
                return R.fail("账号已被禁用");
            }
            
            // 2. 登录
            StpUtil.login(user.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            
            // 3. 更新用户 Token 和过期时间到数据库
            long timeoutSeconds = tokenInfo.getTokenTimeout();
            LocalDateTime expireTime;
            if (timeoutSeconds == -1) {
                expireTime = LocalDateTime.now().plusYears(100); 
            } else {
                expireTime = LocalDateTime.now().plusSeconds(timeoutSeconds);
            }
            
            User userUpdate = new User();
            userUpdate.setId(user.getId());
            userUpdate.setToken(tokenInfo.getTokenValue());
            userUpdate.setTokenExpireTime(expireTime);
            userService.updateById(userUpdate);
            
            // 4. 获取角色信息
            Long roleId = userService.getRoleIdByUserId(user.getId());
            Role role = null;
            if (roleId != null) {
                role = roleMapper.selectById(roleId);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("tokenInfo", tokenInfo);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("roleType", role != null ? role.getRoleKey() : "USER");
            
            return R.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(500, "Login Error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return R.ok();
    }
}
