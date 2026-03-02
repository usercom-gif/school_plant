package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PlantStatusRequest {
    @NotNull(message = "植物ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private String status; // AVAILABLE, ADOPTED
}
