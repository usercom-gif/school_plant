package com.schoolplant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateUpdateRequest extends TemplateAddRequest {
    @NotNull(message = "模板ID不能为空")
    private Long id;
}
