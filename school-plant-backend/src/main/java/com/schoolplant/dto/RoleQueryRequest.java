package com.schoolplant.dto;

import lombok.Data;

@Data
public class RoleQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String roleName;
    private String description;
    private Integer status;
}
