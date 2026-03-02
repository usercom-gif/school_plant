package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("plant_health_reports")
public class HealthReport implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long plantId;
    private Long reporterId; // 上报人ID
    private String description;
    private String imageUrls; // JSON array
    private String aiAnalysis; // AI result
    private String status; // PENDING, PROCESSING, RESOLVED
    private Long handlerId; // 后勤人员ID
    private LocalDateTime handledAt;
    private Integer isReminded; // 0:未提醒, 1:已二次提醒
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
