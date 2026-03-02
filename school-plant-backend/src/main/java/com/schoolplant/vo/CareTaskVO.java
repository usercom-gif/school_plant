package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CareTaskVO {
    private Long id;
    private Long plantId;
    private Long userId; // adopter_id
    
    private String plantName;
    private String userName; // adopter name
    
    private String taskType;
    private String taskDescription;
    private LocalDate dueDate;
    private LocalDate completedDate;
    private String status; // PENDING, COMPLETED, OVERDUE
    private String imageUrl;
    
    private LocalDateTime createdAt;
}
