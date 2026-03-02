package com.schoolplant.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleVO {
    private Long id;
    private String roleKey;
    private String roleName;
    private String description;
    private Integer status;
    private Integer isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
