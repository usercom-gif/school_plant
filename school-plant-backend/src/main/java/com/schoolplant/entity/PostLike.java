package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞表
 */
@Data
@TableName("post_likes")
public class PostLike implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT) // Composite key handled manually or via wrapper
    private Long postId;

    private Long userId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
