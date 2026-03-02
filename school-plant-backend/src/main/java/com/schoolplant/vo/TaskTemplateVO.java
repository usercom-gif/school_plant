package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskTemplateVO {
    private Long id;
    private String plantSpecies;
    private String taskType;
    private String taskDescription;
    private Integer frequencyDays;
    private Integer durationMinutes;
    private String seasonality;
    private String operationRequirements;
    private String scoreStandard;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
