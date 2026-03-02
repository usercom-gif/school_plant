package com.schoolplant.dto;

import lombok.Data;

@Data
public class KnowledgePostQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private String tag;
    private String status; // ACTIVE, PENDING, REJECTED
    private Long authorId;
}
