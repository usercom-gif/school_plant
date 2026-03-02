package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class RoleStatusRequest {
    @NotNull(message = "角色ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
