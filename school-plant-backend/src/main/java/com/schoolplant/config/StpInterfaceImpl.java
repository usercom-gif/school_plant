package com.schoolplant.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.schoolplant.entity.Permission;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.RolePermission;
import com.schoolplant.mapper.PermissionMapper;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.mapper.RolePermissionMapper;
import com.schoolplant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 返回一个账号所拥有的权限码集合 
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        try {
            Long userId = Long.parseLong(loginId.toString());
            Long roleId = userService.getRoleIdByUserId(userId);
            if (roleId != null) {
                Role role = roleMapper.selectById(roleId);
                if (role == null || (role.getStatus() != null && role.getStatus() == 0)) {
                    return list;
                }

                List<RolePermission> rps = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId)
                );
                
                if (!rps.isEmpty()) {
                    List<Long> pids = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
                    List<Permission> perms = permissionMapper.selectBatchIds(pids);
                    if (perms != null) {
                        list = perms.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        try {
            Long userId = Long.parseLong(loginId.toString());
            Long roleId = userService.getRoleIdByUserId(userId);
            if (roleId != null) {
                Role role = roleMapper.selectById(roleId);
                if (role != null && (role.getStatus() == null || role.getStatus() == 1)) {
                    list.add(role.getRoleKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
