package com.schoolplant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;
    private String email;
    private Long roleId; // Simplified role assignment
    private Integer status = 1;
}
