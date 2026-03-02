package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识分享表
 */
@Data
@TableName("knowledge_posts")
public class KnowledgePost implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("author_id")
    private Long authorId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("tag")
    private String tag;

    @TableField("is_featured")
    private Integer isFeatured;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
