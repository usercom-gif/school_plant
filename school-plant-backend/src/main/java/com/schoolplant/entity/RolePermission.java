package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联表
 */
@Data
@TableName("role_permissions")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long permissionId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
