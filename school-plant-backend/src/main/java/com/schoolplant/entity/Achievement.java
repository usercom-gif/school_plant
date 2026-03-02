package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 认养成果与评比表
 */
@Data
@TableName("achievements")
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("plant_id")
    private Long plantId;

    @TableField("adoption_start_date")
    private LocalDate adoptionStartDate;

    @TableField("adoption_end_date")
    private LocalDate adoptionEndDate;

    @TableField("tasks_completed")
    private Integer tasksCompleted;

    @TableField("total_tasks")
    private Integer totalTasks;

    @TableField("task_completion_rate")
    private BigDecimal taskCompletionRate;

    @TableField("plant_health_score")
    private Integer plantHealthScore;

    @TableField("is_outstanding")
    private Integer isOutstanding;

    @TableField("certificate_url")
    private String certificateUrl;

    @TableField("semester")
    private String semester;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Integer getCompletedTasks() {
        return tasksCompleted;
    }

    public void setCompletedTasks(Integer completedTasks) {
        this.tasksCompleted = completedTasks;
    }

    public BigDecimal getCompletionRate() {
        return taskCompletionRate;
    }

    public void setCompletionRate(BigDecimal taskCompletionRate) {
        this.taskCompletionRate = taskCompletionRate;
    }
}
