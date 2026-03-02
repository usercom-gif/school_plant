package com.schoolplant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditApplicationRequest {
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @NotBlank(message = "审核动作不能为空 (PASS/REJECT)")
    private String action;

    private String comment; // 审核意见
}
