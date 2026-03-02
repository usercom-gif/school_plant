package com.schoolplant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String phone;
    private String email;
    private Long roleId;
    private Integer status;
    // Real name cannot be updated directly here, use approval process
    // But admin can modify directly except real name.
    // Wait, prompt says: "Allow administrators to modify user's basic information (except real name can be directly modified)"
    // The prompt is: "允许管理员修改用户的基本信息（除真实姓名外可直接修改）"
    // This usually means: Admin can modify phone, email, status directly. But for real name, maybe not directly?
    // But usually admins can do anything.
    // I'll stick to: Admin can modify phone/email/status. Real name is separate flow or just excluded from this DTO to be safe.
    // If admin really needs to change real name, maybe they can use the same approval flow or a special override.
    // I will exclude realName from here to enforce the rule.
}
