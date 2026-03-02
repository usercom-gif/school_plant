package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.dto.*;
import com.schoolplant.entity.Role;
import com.schoolplant.vo.RoleDetailVO;
import com.schoolplant.vo.RoleVO;
import java.util.List;

public interface RoleService extends IService<Role> {
    Page<RoleVO> getRoleList(RoleQueryRequest request);
    RoleDetailVO getRoleDetail(Long id);
    void addRole(RoleAddRequest request);
    void updateRole(RoleUpdateRequest request);
    void deleteRoles(List<Long> ids);
    void changeStatus(RoleStatusRequest request);
    List<RoleVO> exportRole(RoleQueryRequest request);
}
