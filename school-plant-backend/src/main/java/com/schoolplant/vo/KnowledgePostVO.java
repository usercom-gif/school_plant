package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgePostVO {
    private Long id;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private String tag;
    private Integer isFeatured;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private Boolean hasLiked; // Current user liked?
}
