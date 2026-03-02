package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 养护任务模板表
 */
@Data
@TableName("task_templates")
public class TaskTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("plant_species")
    private String plantSpecies;

    @TableField("task_type")
    private String taskType;

    @TableField("task_description")
    private String taskDescription;

    @TableField("frequency_days")
    private Integer frequencyDays;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("seasonality")
    private String seasonality;

    @TableField("operation_requirements")
    private String operationRequirements;

    @TableField("score_standard")
    private String scoreStandard;

    @TableField("status")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
