package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.dto.*;
import com.schoolplant.service.RoleService;
import com.schoolplant.vo.RoleDetailVO;
import com.schoolplant.vo.RoleExcelVO;
import com.schoolplant.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "System Role", description = "系统角色管理")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "获取角色列表")
    @SaCheckPermission("system:role:manage")
    @GetMapping("/list")
    public R<Page<RoleVO>> list(RoleQueryRequest request) {
        return R.ok(roleService.getRoleList(request));
    }

    @Operation(summary = "获取角色详情")
    @SaCheckPermission("system:role:manage")
    @GetMapping("/{id}")
    public R<RoleDetailVO> getInfo(@PathVariable Long id) {
        return R.ok(roleService.getRoleDetail(id));
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:manage")
    @Log(module = "ROLE", title = "新增角色", operationType = "INSERT")
    @PostMapping
    public R<Void> add(@Validated @RequestBody RoleAddRequest request) {
        roleService.addRole(request);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:manage")
    @Log(module = "ROLE", title = "修改角色", operationType = "UPDATE")
    @PutMapping
    public R<Void> update(@Validated @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(request);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:manage")
    @Log(module = "ROLE", title = "删除角色", operationType = "DELETE")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        roleService.deleteRoles(ids);
        return R.ok();
    }

    @Operation(summary = "修改角色状态")
    @SaCheckPermission("system:role:manage")
    @Log(module = "ROLE", title = "修改角色状态", operationType = "UPDATE")
    @PutMapping("/status")
    public R<Void> changeStatus(@Validated @RequestBody RoleStatusRequest request) {
        roleService.changeStatus(request);
        return R.ok();
    }

    @Operation(summary = "导出角色")
    @SaCheckPermission("system:role:manage")
    @GetMapping("/export")
    public void export(HttpServletResponse response, RoleQueryRequest request) throws IOException {
        List<RoleVO> list = roleService.exportRole(request);
        List<RoleExcelVO> exportList = list.stream().map(vo -> {
            RoleExcelVO excelVO = new RoleExcelVO();
            BeanUtils.copyProperties(vo, excelVO);
            return excelVO;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("角色数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), RoleExcelVO.class).sheet("角色列表").doWrite(exportList);
    }
}
