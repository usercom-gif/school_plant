package com.schoolplant.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.schoolplant.common.R;
import com.schoolplant.dto.AdoptionApplyRequest;
import com.schoolplant.dto.AdoptionStatusResponse;
import com.schoolplant.entity.AdoptionRecord;
import com.schoolplant.service.AdoptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.schoolplant.annotation.Log;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.schoolplant.dto.AdoptionQueryRequest;
import com.schoolplant.dto.AuditApplicationRequest;
import com.schoolplant.vo.AdoptionApplicationVO;
import com.schoolplant.entity.AdoptionAuditLog;
import java.util.List;

@RestController
@RequestMapping("/adoption")
public class AdoptionController {

    @Autowired
    private AdoptionService adoptionService;

    // 检查用户认养状态
    @SaCheckLogin
    @GetMapping("/status")
    public R<AdoptionStatusResponse> checkStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(adoptionService.checkStatus(userId));
    }

    // 提交认养申请
    @SaCheckLogin
    @Log(module = "ADOPTION", title = "提交认养申请", operationType = "INSERT")
    @PostMapping("/submit")
    public R<Long> submit(@Validated @RequestBody AdoptionApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(adoptionService.submitApplication(userId, request));
    }

    // 申请认养 (Old, keep for compatibility or admin direct assign)
    @SaCheckLogin
    @Log(module = "ADOPTION", title = "申请认养", operationType = "INSERT")
    @PostMapping("/apply")
    public R<Long> apply(@RequestParam Long plantId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(adoptionService.applyAdoption(userId, plantId));
    }

    // 我的认养记录 (已通过的)
    @SaCheckLogin
    @GetMapping("/my")
    public R<Page<AdoptionRecord>> myAdoptions(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<AdoptionRecord> pageParam = new Page<>(page, size);
        return R.ok(adoptionService.page(pageParam, 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AdoptionRecord>().eq("user_id", userId)));
    }
    
    // 我的申请记录 (所有状态)
    @SaCheckLogin
    @GetMapping("/my-applications")
    public R<Page<AdoptionApplicationVO>> myApplications(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        AdoptionQueryRequest request = new AdoptionQueryRequest();
        request.setUserId(userId);
        request.setPage(page);
        request.setSize(size);
        return R.ok(adoptionService.listApplicationVOs(request));
    }

    // 取消认养
    @SaCheckLogin
    @Log(module = "ADOPTION", title = "取消认养", operationType = "UPDATE")
    @PostMapping("/cancel")
    public R<Void> cancel(@RequestParam Long recordId, @RequestParam String reason) {
        adoptionService.cancelAdoption(recordId, reason);
        return R.ok();
    }

    // --- Audit Endpoints (Admin Only) ---

    @SaCheckRole("ADMIN")
    @GetMapping("/audit/pending-count")
    public R<Long> getPendingAuditCount() {
        return R.ok(adoptionService.countPendingAudits());
    }

    @SaCheckLogin
    @GetMapping("/audit/list")
    public R<Page<AdoptionApplicationVO>> listAuditApplications(AdoptionQueryRequest request) {
        // 如果不是管理员，也不传 userId，则只能查自己的植物申请
        // 如果是管理员，默认查所有
        // 逻辑已在 Service 层处理
        return R.ok(adoptionService.listApplicationVOs(request));
    }

    @SaCheckLogin
    @Log(module = "ADOPTION", title = "审核认养申请", operationType = "AUDIT")
    @PostMapping("/audit/action")
    public R<Void> auditApplication(@Validated @RequestBody AuditApplicationRequest request) {
        Long adminId = StpUtil.getLoginIdAsLong();
        adoptionService.auditApplication(adminId, request);
        return R.ok();
    }

    @SaCheckLogin // User can also view logs of their application? Maybe. Or Admin only. Let's allow login users to see why rejected.
    @GetMapping("/audit/logs/{applicationId}")
    public R<List<AdoptionAuditLog>> listAuditLogs(@PathVariable Long applicationId) {
        return R.ok(adoptionService.listAuditLogs(applicationId));
    }
}
