package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class TemplateStatusRequest {
    @NotNull(message = "模板ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status; // 1-Enable, 0-Disable
}
