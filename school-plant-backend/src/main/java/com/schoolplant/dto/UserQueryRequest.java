package com.schoolplant.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserQueryRequest {
    private String username;
    private String realName;
    private String email;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 10;
}
