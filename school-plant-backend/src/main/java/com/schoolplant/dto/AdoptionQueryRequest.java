package com.schoolplant.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdoptionQueryRequest {
    private Long userId;
    private Long plantId;
    private String status; // PENDING, INITIAL_PASSED, REVIEW_PASSED, APPROVED, REJECTED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private Integer page = 1;
    private Integer size = 10;
}
