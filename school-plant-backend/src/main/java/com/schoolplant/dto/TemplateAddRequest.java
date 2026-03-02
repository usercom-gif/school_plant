package com.schoolplant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class TemplateAddRequest {
    @NotBlank(message = "植物品种不能为空")
    private String plantSpecies;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @NotBlank(message = "任务描述不能为空")
    private String taskDescription;

    @NotNull(message = "任务周期不能为空")
    @Min(value = 1, message = "任务周期必须大于等于1天")
    private Integer frequencyDays;

    private Integer durationMinutes;
    private String seasonality;
    private String operationRequirements;
    private String scoreStandard;
    private Integer status; // 1-Enable, 0-Disable
}
