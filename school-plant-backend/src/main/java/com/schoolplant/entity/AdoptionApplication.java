package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 认养申请表
 */
@Data
@TableName("adoption_applications")
public class AdoptionApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("plant_id")
    private Long plantId;

    @TableField("adoption_period_months")
    private Integer adoptionPeriodMonths;

    @TableField("care_experience")
    private String careExperience;

    @TableField("status")
    private String status;

    @TableField("rejection_reason")
    private String rejectionReason;

    @TableField("approved_by")
    private Long approvedBy;

    @TableField("approved_at")
    private LocalDateTime approvedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
