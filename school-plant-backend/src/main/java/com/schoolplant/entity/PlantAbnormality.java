package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 植物健康异常表
 */
@Data
@TableName("plant_abnormalities")
public class PlantAbnormality implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("plant_id")
    private Long plantId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("maintainer_id")
    private Long maintainerId;

    @TableField("abnormality_type")
    private String abnormalityType;

    @TableField("description")
    private String description;

    @TableField("image_urls")
    private String imageUrls; // JSON array

    @TableField("suggested_solution")
    private String suggestedSolution;

    @TableField("status")
    private String status;

    @TableField("assigned_at")
    private LocalDateTime assignedAt;

    @TableField("resolution_description")
    private String resolutionDescription;

    @TableField("resolution_image_urls")
    private String resolutionImageUrls;

    @TableField("materials_used")
    private String materialsUsed;

    @TableField("effect_evaluation")
    private String effectEvaluation;

    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    @TableField("overtime_alert_sent")
    private Integer overtimeAlertSent;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
