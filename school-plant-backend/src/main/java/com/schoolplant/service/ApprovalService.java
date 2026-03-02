package com.schoolplant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.schoolplant.entity.Approval;

public interface ApprovalService extends IService<Approval> {
    /**
     * 提交真实姓名修改申请
     */
    void submitRealNameChange(Long userId, String newName, String reason);

    /**
     * 审批申请
     */
    void handleApproval(Long adminId, Long approvalId, String status, String comment);
}
