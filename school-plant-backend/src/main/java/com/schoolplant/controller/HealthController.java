package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.common.R;
import com.schoolplant.entity.HealthReport;
import com.schoolplant.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    // 上报异常 (普通用户)
    @SaCheckLogin
    @PostMapping("/report")
    public R<Long> report(@RequestBody HealthReport report) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(healthService.report(userId, report.getPlantId(), report.getDescription(), report.getImageUrls()));
    }

    // 处理异常 (后勤人员)
    @SaCheckRole("GARDENER")
    @PostMapping("/resolve")
    public R<Void> resolve(@RequestParam Long reportId, @RequestParam String result) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        healthService.resolve(reportId, handlerId, result);
        return R.ok();
    }

    // 待处理异常列表 (后勤)
    @SaCheckRole("GARDENER")
    @GetMapping("/pending")
    public R<Page<HealthReport>> pending(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Page<HealthReport> pageParam = new Page<>(page, size);
        return R.ok(healthService.page(pageParam, 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HealthReport>().eq("status", "PENDING")));
    }
}
