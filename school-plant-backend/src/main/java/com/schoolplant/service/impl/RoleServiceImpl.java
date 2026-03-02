package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.dto.*;
import com.schoolplant.entity.Permission;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.RolePermission;
import com.schoolplant.entity.UserRole;
import com.schoolplant.mapper.PermissionMapper;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.mapper.RolePermissionMapper;
import com.schoolplant.mapper.UserRoleMapper;
import com.schoolplant.service.RoleService;
import com.schoolplant.vo.RoleDetailVO;
import com.schoolplant.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Page<RoleVO> getRoleList(RoleQueryRequest request) {
        Page<Role> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getRoleName())) {
            wrapper.like(Role::getRoleName, request.getRoleName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            wrapper.like(Role::getDescription, request.getDescription());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Role::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Role::getCreatedAt);

        Page<Role> rolePage = this.page(page, wrapper);
        
        Page<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> voList = rolePage.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public RoleDetailVO getRoleDetail(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        RoleDetailVO vo = new RoleDetailVO();
        BeanUtils.copyProperties(role, vo);

        List<RolePermission> rps = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id)
        );
        vo.setPermissionIds(rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleAddRequest request) {
        if (this.count(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, request.getRoleKey())) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        if (this.count(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, request.getRoleName())) > 0) {
            throw new RuntimeException("角色名称已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setIsSystemRole(0);
        if (role.getStatus() == null) role.setStatus(1);
        this.save(role);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            // Filter valid permission IDs to avoid foreign key constraint violation
            List<Long> validPids = permissionMapper.selectBatchIds(request.getPermissionIds())
                    .stream().map(Permission::getId).collect(Collectors.toList());

            for (Long pid : validPids) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateRequest request) {
        Role role = this.getById(request.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        if (this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleKey, request.getRoleKey())
                .ne(Role::getId, request.getId())) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        if (this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, request.getRoleName())
                .ne(Role::getId, request.getId())) > 0) {
            throw new RuntimeException("角色名称已存在");
        }

        if (role.getIsSystemRole() == 1) {
            if (!role.getRoleKey().equals(request.getRoleKey())) {
                 throw new RuntimeException("系统内置角色无法修改角色编码");
            }
        }

        BeanUtils.copyProperties(request, role);
        this.updateById(role);

        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId()));
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Long> validPids = permissionMapper.selectBatchIds(request.getPermissionIds())
                    .stream().map(Permission::getId).collect(Collectors.toList());

            for (Long pid : validPids) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> ids) {
        for (Long id : ids) {
            Role role = this.getById(id);
            if (role != null) {
                if (role.getIsSystemRole() == 1) {
                    throw new RuntimeException("系统内置角色无法删除: " + role.getRoleName());
                }
                Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
                if (userCount > 0) {
                    throw new RuntimeException("角色 " + role.getRoleName() + " 已分配给用户，无法删除");
                }
                
                rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
                // Use mapper directly instead of this.removeById(id) to avoid TableInfo NPE in tests
                this.baseMapper.deleteById(id);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(RoleStatusRequest request) {
        Role role = this.getById(request.getId());
        if (role != null) {
            role.setStatus(request.getStatus());
            this.updateById(role);
        }
    }

    @Override
    public List<RoleVO> exportRole(RoleQueryRequest request) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getRoleName())) {
            wrapper.like(Role::getRoleName, request.getRoleName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            wrapper.like(Role::getDescription, request.getDescription());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Role::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        
        List<Role> list = this.list(wrapper);
        return list.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
