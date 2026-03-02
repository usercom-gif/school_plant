package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联表
 */
@Data
@TableName("user_roles")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableField("user_id") // 明确指定数据库字段名
    private Long userId;

    @TableField("role_id") // 明确指定数据库字段名
    private Long roleId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
