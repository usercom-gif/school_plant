package com.schoolplant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.schoolplant.entity.Approval;
import com.schoolplant.entity.User;
import com.schoolplant.mapper.ApprovalMapper;
import com.schoolplant.mapper.UserMapper;
import com.schoolplant.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApprovalServiceImpl extends ServiceImpl<ApprovalMapper, Approval> implements ApprovalService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitRealNameChange(Long userId, String newName, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否有待审批的申请
        Long count = this.lambdaQuery()
                .eq(Approval::getUserId, userId)
                .eq(Approval::getType, "REAL_NAME_CHANGE")
                .eq(Approval::getStatus, "PENDING")
                .count();
        if (count > 0) {
            throw new RuntimeException("您已有待审批的更名申请，请勿重复提交");
        }

        Approval approval = new Approval();
        approval.setUserId(userId);
        approval.setType("REAL_NAME_CHANGE");
        approval.setOriginalValue(user.getRealName());
        approval.setNewValue(newName);
        approval.setReason(reason);
        approval.setStatus("PENDING");
        approval.setCreatedAt(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());

        this.save(approval);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(Long adminId, Long approvalId, String status, String comment) {
        Approval approval = this.getById(approvalId);
        if (approval == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }

        approval.setStatus(status);
        approval.setApproverId(adminId);
        approval.setComment(comment);
        approval.setUpdatedAt(LocalDateTime.now());
        this.updateById(approval);

        // 如果通过，更新用户信息
        if ("APPROVED".equals(status)) {
            if ("REAL_NAME_CHANGE".equals(approval.getType())) {
                User user = userMapper.selectById(approval.getUserId());
                if (user != null) {
                    user.setRealName(approval.getNewValue());
                    userMapper.updateById(user);
                }
            }
        }
    }
}
