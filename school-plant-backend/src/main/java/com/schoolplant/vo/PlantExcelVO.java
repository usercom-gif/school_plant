package com.schoolplant.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.Year;

@Data
public class PlantExcelVO {
    @ExcelProperty("植物编号")
    private String plantCode;

    @ExcelProperty("植物名称")
    private String name;

    @ExcelProperty("植物品种")
    private String species;

    @ExcelProperty("科属")
    private String family;

    @ExcelProperty("位置描述")
    private String locationDescription;

    @ExcelProperty("校园区域")
    private String region;

    @ExcelProperty("养护难度等级")
    private Integer careDifficulty;

    @ExcelProperty("植物状态")
    private String status;

    @ExcelProperty("种植年份")
    private Integer plantingYear;

    @ExcelProperty("光照需求")
    private String lightRequirement;

    @ExcelProperty("水分需求")
    private String waterRequirement;

    @ExcelProperty("植物详细描述")
    private String description;

    @ExcelProperty("养护要点")
    private String careTips;

    @ExcelProperty("生长周期")
    private String growthCycle;

    @ExcelProperty("植物数量")
    private Long number;
    
    @ExcelProperty("创建时间")
    private LocalDateTime createdAt;
}
