package com.schoolplant.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleExcelVO {
    @ExcelProperty("角色ID")
    private Long id;
    
    @ExcelProperty("角色编码")
    private String roleKey;
    
    @ExcelProperty("角色名称")
    private String roleName;
    
    @ExcelProperty("描述")
    private String description;
    
    @ExcelProperty("状态(1-启用,0-禁用)")
    private Integer status;
    
    @ExcelProperty("创建时间")
    private LocalDateTime createdAt;
}
