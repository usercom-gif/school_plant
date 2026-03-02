package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.dto.*;
import com.schoolplant.entity.Role;
import com.schoolplant.entity.RolePermission;
import com.schoolplant.entity.UserRole;
import com.schoolplant.mapper.RoleMapper;
import com.schoolplant.mapper.RolePermissionMapper;
import com.schoolplant.mapper.UserRoleMapper;
import com.schoolplant.vo.RoleDetailVO;
import com.schoolplant.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @BeforeEach
    public void setup() {
        // Fix: Inject mocked mapper into ServiceImpl
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    @Test
    public void testGetRoleList() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setSize(10);
        request.setRoleName("Test");

        Role role = new Role();
        role.setId(1L);
        role.setRoleName("Test Role");

        Page<Role> page = new Page<>();
        page.setRecords(Collections.singletonList(role));
        page.setTotal(1);

        when(roleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<RoleVO> result = roleService.getRoleList(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("Test Role", result.getRecords().get(0).getRoleName());
    }

    @Test
    public void testGetRoleDetail() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("Test Role");

        RolePermission rp = new RolePermission();
        rp.setPermissionId(100L);

        when(roleMapper.selectById(1L)).thenReturn(role);
        when(rolePermissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(rp));

        RoleDetailVO result = roleService.getRoleDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getPermissionIds().size());
        assertEquals(100L, result.getPermissionIds().get(0));
    }

    @Test
    public void testAddRole_Success() {
        RoleAddRequest request = new RoleAddRequest();
        request.setRoleName("New Role");
        request.setRoleKey("NEW_ROLE");
        request.setPermissionIds(Collections.singletonList(1L));

        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(roleMapper.insert(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(1L);
            return 1;
        });

        roleService.addRole(request);

        verify(roleMapper, times(1)).insert(any(Role.class));
        verify(rolePermissionMapper, times(1)).insert(any(RolePermission.class));
    }

    @Test
    public void testAddRole_DuplicateKey() {
        RoleAddRequest request = new RoleAddRequest();
        request.setRoleKey("EXISTING");

        // Mock count > 0 for key check
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> roleService.addRole(request));
    }

    @Test
    public void testUpdateRole_Success() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(1L);
        request.setRoleName("Updated Role");
        request.setRoleKey("UPDATED");
        request.setPermissionIds(Collections.singletonList(2L));

        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setRoleKey("OLD");
        existingRole.setIsSystemRole(0);

        when(roleMapper.selectById(1L)).thenReturn(existingRole);
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // Unique checks

        roleService.updateRole(request);

        verify(roleMapper, times(1)).updateById(any(Role.class));
        verify(rolePermissionMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, times(1)).insert(any(RolePermission.class));
    }

    @Test
    public void testUpdateRole_SystemRoleKeyChange() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(1L);
        request.setRoleKey("NEW_KEY");

        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setRoleKey("ADMIN");
        existingRole.setIsSystemRole(1);

        when(roleMapper.selectById(1L)).thenReturn(existingRole);
        when(roleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThrows(RuntimeException.class, () -> roleService.updateRole(request));
    }

    @Test
    public void testDeleteRoles_Success() {
        Role role = new Role();
        role.setId(1L);
        role.setIsSystemRole(0);
        role.setRoleName("Custom Role");

        when(roleMapper.selectById(1L)).thenReturn(role);
        when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        roleService.deleteRoles(Collections.singletonList(1L));

        verify(roleMapper, times(1)).deleteById(1L);
        verify(rolePermissionMapper, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    public void testDeleteRoles_SystemRole() {
        Role role = new Role();
        role.setId(1L);
        role.setIsSystemRole(1);

        when(roleMapper.selectById(1L)).thenReturn(role);

        assertThrows(RuntimeException.class, () -> roleService.deleteRoles(Collections.singletonList(1L)));
    }

    @Test
    public void testChangeStatus() {
        RoleStatusRequest request = new RoleStatusRequest();
        request.setId(1L);
        request.setStatus(0);

        Role role = new Role();
        role.setId(1L);

        when(roleMapper.selectById(1L)).thenReturn(role);

        roleService.changeStatus(request);

        verify(roleMapper, times(1)).updateById(role);
        assertEquals(0, role.getStatus());
    }
}
