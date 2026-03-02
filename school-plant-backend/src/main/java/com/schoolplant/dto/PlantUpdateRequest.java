package com.schoolplant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlantUpdateRequest extends PlantAddRequest {
    @NotNull(message = "植物ID不能为空")
    private Long id;
}
