package com.schoolplant.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserExcelVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("真实姓名")
    private String realName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("状态 (0:禁用 1:启用)")
    private Integer status;

    @ExcelProperty("创建时间")
    private LocalDateTime createdAt;
}
