package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("operation_logs")
public class OperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operator_role")
    private String operatorRole;

    @TableField("module")
    private String module;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_desc")
    private String operationDesc; // Missing field added

    @TableField("operation_content")
    private String operationContent; // Stored as JSON string

    @TableField("operation_result")
    private String operationResult; // SUCCESS, FAILURE

    @TableField("error_msg")
    private String errorMsg;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("execution_time")
    private Long executionTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
