package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户班级关联表
 */
@Data
@TableName("user_classes")
public class UserClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long classId;
}
