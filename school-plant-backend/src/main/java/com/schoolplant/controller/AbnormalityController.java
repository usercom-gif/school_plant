package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.entity.PlantAbnormality;
import com.schoolplant.service.PlantAbnormalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Abnormality", description = "植物异常管理")
@RestController
@RequestMapping("/abnormality")
public class AbnormalityController {

    @Autowired
    private PlantAbnormalityService abnormalityService;

    @Operation(summary = "上报异常")
    @SaCheckLogin
    @Log(module = "ABNORMALITY", title = "上报异常", operationType = "INSERT")
    @PostMapping("/report")
    public R<String> report(
            @RequestParam Long plantId,
            @RequestParam String type,
            @RequestParam String desc,
            @RequestParam(required = false) MultipartFile[] images
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        String result = abnormalityService.reportAbnormality(plantId, userId, type, desc, images == null ? new MultipartFile[0] : images);
        return R.ok(result);
    }

    @Operation(summary = "分派工单")
    @SaCheckRole("ADMIN")
    @Log(module = "ABNORMALITY", title = "分派工单", operationType = "UPDATE")
    @PostMapping("/assign")
    public R<Void> assign(@RequestParam Long id, @RequestParam Long maintainerId) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        abnormalityService.assignMaintainer(id, maintainerId, operatorId);
        return R.ok();
    }

    @Operation(summary = "处理异常")
    @SaCheckRole("MAINTAINER")
    @Log(module = "ABNORMALITY", title = "处理异常", operationType = "UPDATE")
    @PostMapping("/resolve")
    public R<Void> resolve(
            @RequestParam Long id,
            @RequestParam String resolution,
            @RequestParam String materials,
            @RequestParam String evaluation,
            @RequestParam(required = false) MultipartFile[] images
    ) {
        abnormalityService.resolveAbnormality(id, resolution, materials, evaluation, images);
        return R.ok();
    }

    @Operation(summary = "查询异常列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<PlantAbnormality>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long maintainerId,
            @RequestParam(required = false) Long reporterId
    ) {
        Page<PlantAbnormality> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PlantAbnormality> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(PlantAbnormality::getStatus, status);
        }
        if (maintainerId != null) {
            wrapper.eq(PlantAbnormality::getMaintainerId, maintainerId);
        }
        if (reporterId != null) {
            wrapper.eq(PlantAbnormality::getReporterId, reporterId);
        }
        
        // RBAC Filter
        if (StpUtil.hasRole("USER")) {
            // Users only see their own reports? Requirement: "View their own history"
            wrapper.eq(PlantAbnormality::getReporterId, StpUtil.getLoginIdAsLong());
        } else if (StpUtil.hasRole("MAINTAINER")) {
            // Maintainers see assigned to them
            wrapper.eq(PlantAbnormality::getMaintainerId, StpUtil.getLoginIdAsLong());
        }
        // Admin sees all (no extra filter)

        wrapper.orderByDesc(PlantAbnormality::getCreatedAt);
        return R.ok(abnormalityService.page(pageParam, wrapper));
    }
    
    @Operation(summary = "获取异常详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<PlantAbnormality> getDetail(@PathVariable Long id) {
        return R.ok(abnormalityService.getById(id));
    }
}
