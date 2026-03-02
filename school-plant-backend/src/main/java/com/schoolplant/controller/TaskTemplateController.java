package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.dto.*;
import com.schoolplant.service.TaskTemplateService;
import com.schoolplant.vo.TaskTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task Template Management", description = "养护任务模板管理")
@RestController
@RequestMapping("/task/template")
public class TaskTemplateController {

    @Autowired
    private TaskTemplateService taskTemplateService;

    @Operation(summary = "获取模板列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<TaskTemplateVO>> list(TemplateQueryRequest request) {
        // If not admin, only show enabled templates
        if (!StpUtil.hasRole("ADMIN")) {
            request.setStatus(1);
        }
        return R.ok(taskTemplateService.getTemplateList(request));
    }

    @Operation(summary = "获取模板详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<TaskTemplateVO> getInfo(@PathVariable Long id) {
        return R.ok(taskTemplateService.getTemplateDetail(id));
    }

    @Operation(summary = "新增模板")
    @SaCheckRole("ADMIN")
    @Log(module = "TASK", title = "新增任务模板", operationType = "INSERT")
    @PostMapping
    public R<Void> add(@Validated @RequestBody TemplateAddRequest request) {
        taskTemplateService.addTemplate(request);
        return R.ok();
    }

    @Operation(summary = "修改模板")
    @SaCheckRole("ADMIN")
    @Log(module = "TASK", title = "修改任务模板", operationType = "UPDATE")
    @PutMapping
    public R<Void> update(@Validated @RequestBody TemplateUpdateRequest request) {
        taskTemplateService.updateTemplate(request);
        return R.ok();
    }

    @Operation(summary = "删除模板")
    @SaCheckRole("ADMIN")
    @Log(module = "TASK", title = "删除任务模板", operationType = "DELETE")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        taskTemplateService.deleteTemplates(ids);
        return R.ok();
    }

    @Operation(summary = "修改模板状态")
    @SaCheckRole("ADMIN")
    @Log(module = "TASK", title = "修改任务模板状态", operationType = "UPDATE")
    @PutMapping("/status")
    public R<Void> changeStatus(@Validated @RequestBody TemplateStatusRequest request) {
        taskTemplateService.changeStatus(request);
        return R.ok();
    }
}
