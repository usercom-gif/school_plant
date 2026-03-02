package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_likes")
public class KnowledgeLike implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shareId;
    private Long userId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
