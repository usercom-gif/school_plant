package com.schoolplant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Year;

/**
 * 植物信息表
 */
@Data
@TableName("plants")
public class Plant implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    @TableId(type = IdType.AUTO)
    private Long id;

    @ExcelProperty("植物编号")
    @TableField("plant_code")
    private String plantCode;

    @ExcelProperty("植物名称")
    @TableField("name")
    private String name;

    @ExcelProperty("植物品种")
    @TableField("species")
    private String species;

    @ExcelProperty("科属")
    @TableField("family")
    private String family;

    @ExcelProperty("位置描述")
    @TableField("location_description")
    private String locationDescription;

    @ExcelProperty("校园区域")
    @TableField("region")
    private String region;

    @ExcelProperty("养护难度等级")
    @TableField("care_difficulty")
    private Integer careDifficulty;

    @ExcelProperty("植物状态")
    @TableField("status")
    private String status;

    @ExcelProperty("种植年份")
    @TableField("planting_year")
    private Year plantingYear;

    @ExcelProperty("光照需求")
    @TableField("light_requirement")
    private String lightRequirement;

    @ExcelProperty("水分需求")
    @TableField("water_requirement")
    private String waterRequirement;

    @ExcelProperty("植物详细描述")
    @TableField("description")
    private String description;

    @ExcelProperty("植物图片URL")
    @TableField("image_urls")
    private String imageUrls; // JSON array

    @ExcelIgnore
    @TableField("created_by")
    private Long createdBy;

    @ExcelIgnore
    @TableField("number")
    private Long number;

    @ExcelProperty("养护要点")
    @TableField("care_tips")
    private String careTips;

    @ExcelProperty("生长周期")
    @TableField("growth_cycle")
    private String growthCycle;

    @ExcelProperty("发布者姓名")
    @TableField("user_real_name")
    private String userRealName;

    @ExcelProperty("发布者电话")
    @TableField("user_phone")
    private String userPhone;

    @ExcelIgnore
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @ExcelIgnore
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @ExcelIgnore
    @TableField("deleted_at")
    @TableLogic
    private LocalDateTime deletedAt;
    
    // Add imageUrl field for compatibility if needed, or remove usage
    @TableField(exist = false)
    private String imageUrl; // For simple usage
}
