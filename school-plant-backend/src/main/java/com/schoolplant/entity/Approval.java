package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批记录表
 */
@Data
@TableName("sys_approval")
public class Approval implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 审批类型: REAL_NAME_CHANGE
     */
    @TableField("type")
    private String type;

    /**
     * 原值
     */
    @TableField("original_value")
    private String originalValue;

    /**
     * 新值
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 申请理由
     */
    @TableField("reason")
    private String reason;

    /**
     * 状态: PENDING, APPROVED, REJECTED
     */
    @TableField("status")
    private String status;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批意见
     */
    @TableField("comment")
    private String comment;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
