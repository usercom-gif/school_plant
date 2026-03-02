package com.schoolplant.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private String account; // username
    private String name; // realName
    private String idNumber; // studentId or employeeId (mocked for now as we don't have employeeId field, reuse studentId)
    private String phone;
    private String email;
    private String role; // Role Description (普通用户, 管理员, 养护员)
    private String registerTime; // createdAt
    private String avatarUrl;
    private Integer statisticNum; // adoption count / audit count / handled abnormality count
}
