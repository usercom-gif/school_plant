package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.schoolplant.common.R;
import com.schoolplant.dto.DashboardStatItem;
import com.schoolplant.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "仪表盘")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "获取统计数据")
    @SaCheckLogin
    @GetMapping("/stats")
    public R<List<DashboardStatItem>> getStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        // Get role key. StpUtil.getRoleList() returns list. Assuming single role or primary role logic.
        // For simplicity, pick first role or default.
        String roleKey = "USER";
        if (StpUtil.hasRole("ADMIN")) {
            roleKey = "ADMIN";
        } else if (StpUtil.hasRole("MAINTAINER")) {
            roleKey = "MAINTAINER";
        }
        
        return R.ok(dashboardService.getStats(userId, roleKey));
    }
}
