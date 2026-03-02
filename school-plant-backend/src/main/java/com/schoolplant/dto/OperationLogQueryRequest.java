package com.schoolplant.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OperationLogQueryRequest {
    private Long userId; // Operator ID
    private List<String> modules; // Module Enum list
    private String operationType; // Operation Type
    private String operationResult; // SUCCESS/FAILURE
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private Integer page = 1;
    private Integer size = 10;
}
