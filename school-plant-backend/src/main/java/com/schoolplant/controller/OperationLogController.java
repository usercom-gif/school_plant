package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.annotation.Log;
import com.schoolplant.common.R;
import com.schoolplant.dto.OperationLogQueryRequest;
import com.schoolplant.entity.OperationLog;
import com.schoolplant.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Operation Log Management", description = "操作日志管理")
@RestController
@RequestMapping("/system/log")
public class OperationLogController {

    @Autowired
    private OperationLogService logService;

    @Operation(summary = "查询操作日志列表")
    @SaCheckRole("ADMIN") // Only Admin
    @GetMapping("/list")
    public R<Page<OperationLog>> list(OperationLogQueryRequest request) {
        Page<OperationLog> page = logService.listLogs(request);
        System.out.println("Querying logs with request: " + request);
        System.out.println("Found " + page.getTotal() + " records");
        return R.ok(page);
    }

    // Export endpoint could be added here
}
