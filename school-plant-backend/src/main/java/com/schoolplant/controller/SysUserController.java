package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.common.SecurityUtils;
import com.schoolplant.dto.*;
import com.schoolplant.entity.*;
import com.schoolplant.mapper.*;
import com.schoolplant.service.ApprovalService;
import com.schoolplant.service.UserService;
import com.schoolplant.vo.UserExcelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "System User", description = "系统用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserClassMapper userClassMapper;
    
    @Autowired
    private ClazzMapper clazzMapper;
    
    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private ApprovalService approvalService;
    
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        
        // 过滤敏感信息
        if (user != null) {
            user.setPassword(null);
            user.setToken(null);
        }
        
        Long roleId = userService.getRoleIdByUserId(userId);
        Role role = roleMapper.selectById(roleId);
        
        // Fetch Class and Grade info
        String className = null;
        String gradeName = null;
        
        try {
            UserClass userClass = userClassMapper.selectOne(
                new QueryWrapper<UserClass>().eq("user_id", userId).last("LIMIT 1")
            );
            
            if (userClass != null) {
                Clazz clazz = clazzMapper.selectById(userClass.getClassId());
                if (clazz != null) {
                    className = clazz.getName();
                    Grade grade = gradeMapper.selectById(clazz.getGradeId());
                    if (grade != null) {
                        gradeName = grade.getName();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching class info: " + e.getMessage());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("role", role);
        data.put("permissions", new String[]{"*:*:*"}); // Simplified permissions
        data.put("className", className);
        data.put("gradeName", gradeName);
        
        return R.ok(data);
    }

    @Operation(summary = "修改个人信息")
    @Log(module = "USER", title = "修改个人信息", operationType = "UPDATE")
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, request);
        return R.ok();
    }

    @Operation(summary = "修改密码")
    @Log(module = "USER", title = "修改密码", operationType = "UPDATE")
    @PutMapping("/password")
    public R<Void> updatePassword(@Validated @RequestBody UpdatePasswordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updatePassword(userId, request);
        return R.ok();
    }

    // ================= User Management (Admin) =================

    @Operation(summary = "获取用户列表")
    @SaCheckRole("ADMIN")
    @GetMapping("/list")
    public R<Page<User>> list(UserQueryRequest query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 精确或模糊匹配各个字段
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(User::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getRealName())) {
            wrapper.like(User::getRealName, query.getRealName());
        }
        if (StringUtils.hasText(query.getEmail())) {
            wrapper.like(User::getEmail, query.getEmail());
        }
        
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(User::getCreatedAt, query.getStartTime(), query.getEndTime());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = userService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        // Clear sensitive info
        page.getRecords().forEach(u -> {
            u.setPassword(null);
            u.setToken(null);
        });
        return R.ok(page);
    }

    @Operation(summary = "新增用户")
    @SaCheckRole("ADMIN")
    @Log(module = "USER", title = "新增用户", operationType = "INSERT")
    @PostMapping
    public R<Void> add(@Validated @RequestBody UserAddRequest request) {
        User exist = userService.getByUsername(request.getUsername());
        if (exist != null) {
            return R.fail("用户名已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(request, user);
        // Encrypt password (Double MD5)
        String pwd = request.getPassword();
        String encryptedOnce = SecurityUtils.encryptPassword(pwd);
        String encryptedTwice = SecurityUtils.encryptPassword(encryptedOnce);
        user.setPassword(encryptedTwice);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);
        
        // Assign Role
        if (request.getRoleId() != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(request.getRoleId());
            userRoleMapper.insert(ur);
        }
        
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckRole("ADMIN")
    @Log(module = "USER", title = "修改用户", operationType = "UPDATE")
    @PutMapping
    public R<Void> update(@Validated @RequestBody UserUpdateRequest request) {
        User user = userService.getById(request.getId());
        if (user == null) {
            return R.fail("用户不存在");
        }

        // Prevent modifying other ADMINs
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long targetRoleId = userService.getRoleIdByUserId(user.getId());
        if (targetRoleId != null) {
            Role role = roleMapper.selectById(targetRoleId);
            if (role != null && "ADMIN".equals(role.getRoleKey())) {
                if (!user.getId().equals(currentUserId)) {
                    return R.fail("无权修改其他管理员信息");
                }
            }
        }
        
        User update = new User();
        update.setId(request.getId());
        if (request.getPhone() != null) update.setPhone(request.getPhone());
        if (request.getEmail() != null) update.setEmail(request.getEmail());
        if (request.getStatus() != null) update.setStatus(request.getStatus());
        
        userService.updateById(update);
        
        // Update Role if needed
        if (request.getRoleId() != null) {
            userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", request.getId()));
            UserRole ur = new UserRole();
            ur.setUserId(request.getId());
            ur.setRoleId(request.getRoleId());
            userRoleMapper.insert(ur);
        }
        
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckRole("ADMIN")
    @Log(module = "USER", title = "删除用户", operationType = "DELETE")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        // Prevent deleting other admins (simplified check)
        // Check if any user is ADMIN
        for (Long id : ids) {
            Long roleId = userService.getRoleIdByUserId(id);
            if (roleId != null) {
                 Role role = roleMapper.selectById(roleId);
                 if (role != null && "ADMIN".equals(role.getRoleKey())) {
                     // Can't delete admin unless super admin (not impl), 
                     // or just restrict deleting ANY admin to be safe
                     return R.fail("不能删除管理员账户");
                 }
            }
        }
        userService.removeBatchByIds(ids);
        // Delete user roles
        userRoleMapper.delete(new QueryWrapper<UserRole>().in("user_id", ids));
        return R.ok();
    }

    @Operation(summary = "导出用户")
    @SaCheckRole("ADMIN")
    @GetMapping("/export")
    public void export(HttpServletResponse response, UserQueryRequest query) throws IOException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getUsername())) wrapper.like(User::getUsername, query.getUsername());
        if (StringUtils.hasText(query.getRealName())) wrapper.like(User::getRealName, query.getRealName());
        if (StringUtils.hasText(query.getEmail())) wrapper.like(User::getEmail, query.getEmail());
        if (query.getStatus() != null) wrapper.eq(User::getStatus, query.getStatus());
        
        List<User> list = userService.list(wrapper);
        List<UserExcelVO> exportList = list.stream().map(u -> {
            UserExcelVO vo = new UserExcelVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), UserExcelVO.class).sheet("用户列表").doWrite(exportList);
    }

    @Operation(summary = "导入用户")
    @SaCheckRole("ADMIN")
    @Log(module = "USER", title = "导入用户", operationType = "IMPORT")
    @PostMapping("/import")
    public R<String> importData(MultipartFile file) throws IOException {
        List<UserExcelVO> list = EasyExcel.read(file.getInputStream()).head(UserExcelVO.class).sheet().doReadSync();
        // Simple import logic
        int success = 0;
        int fail = 0;
        for (UserExcelVO vo : list) {
             if (userService.getByUsername(vo.getUsername()) != null) {
                 fail++;
                 continue;
             }
             User user = new User();
             BeanUtils.copyProperties(vo, user);
             // Default pwd "123456" with double MD5
             String defaultPwd = "123456";
             String encryptedOnce = SecurityUtils.encryptPassword(defaultPwd);
             String encryptedTwice = SecurityUtils.encryptPassword(encryptedOnce);
             user.setPassword(encryptedTwice); 
             user.setCreatedAt(LocalDateTime.now());
             userService.save(user);
             success++;
        }
        return R.ok("导入成功 " + success + " 条，失败 " + fail + " 条（用户名重复）");
    }

    // ================= Real Name Approval =================

    @Operation(summary = "提交真实姓名修改申请")
    @Log(module = "USER", title = "申请改名", operationType = "INSERT")
    @PostMapping("/submit-name-change")
    public R<Void> submitNameChange(@RequestParam String newName, @RequestParam String reason) {
        Long userId = StpUtil.getLoginIdAsLong();
        approvalService.submitRealNameChange(userId, newName, reason);
        return R.ok();
    }

    @Operation(summary = "审批改名申请")
    @SaCheckRole("ADMIN")
    @Log(module = "USER", title = "审批改名", operationType = "AUDIT")
    @PostMapping("/approve-name-change")
    public R<Void> approveNameChange(@RequestParam Long approvalId, @RequestParam String status, @RequestParam(required = false) String comment) {
        Long adminId = StpUtil.getLoginIdAsLong();
        approvalService.handleApproval(adminId, approvalId, status, comment);
        return R.ok();
    }

    @Operation(summary = "获取审批列表")
    @SaCheckRole("ADMIN")
    @GetMapping("/approvals")
    public R<List<Approval>> listApprovals(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getType, "REAL_NAME_CHANGE");
        if (StringUtils.hasText(status)) {
            wrapper.eq(Approval::getStatus, status);
        }
        wrapper.orderByDesc(Approval::getCreatedAt);
        return R.ok(approvalService.list(wrapper));
    }
}
