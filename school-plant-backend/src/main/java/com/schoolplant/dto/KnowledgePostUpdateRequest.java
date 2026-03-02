package com.schoolplant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgePostUpdateRequest extends KnowledgePostAddRequest {
    @NotNull(message = "ID不能为空")
    private Long id;
}
